package com.shuzijun.leetcode.plugin.window.login;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCookieVisitor;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginPanel extends DialogWrapper {

    private BorderLayoutPanel panel = JBUI.Panels.simplePanel();

    private JTextArea cookieText = new JBTextArea();

    private JcefPanel jcefPanel;

    private Project project;

    private Action okAction;

    public LoginPanel(@Nullable Project project) {
        super(project, null, false, IdeModalityType.IDE, !HttpLogin.isEnabledJcef());
        this.project = project;
        if (HttpLogin.isEnabledJcef()) {
            okAction = new OkAction() {
            };
            try {
                jcefPanel = new JcefPanel(project, okAction);
            } catch (IllegalArgumentException e) {
                jcefPanel = new JcefPanel(project, okAction,true);
            }
            jcefPanel.getComponent().setMinimumSize(new Dimension(1000, 500));
            jcefPanel.getComponent().setPreferredSize(new Dimension(1000, 500));
            panel.addToCenter(new JBScrollPane(jcefPanel.getComponent(), JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        } else {
            cookieText.setLineWrap(true);
            cookieText.setMinimumSize(new Dimension(400, 200));
            cookieText.setPreferredSize(new Dimension(400, 200));
            panel.addToCenter(new JBScrollPane(cookieText, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
            okAction = new OkAction() {
                @Override
                protected void doAction(ActionEvent e) {
                    String cookiesString = cookieText.getText();
                    if (StringUtils.isBlank(cookiesString)) {
                        JOptionPane.showMessageDialog(null, "cookie is null");
                        return;
                    }
                    final List<HttpCookie> cookieList = new ArrayList<>();
                    String[] cookies = cookiesString.split(";");
                    for (String cookieString : cookies) {
                        String[] cookie = cookieString.trim().split("=");
                        if (cookie.length >= 2) {
                            try {
                                HttpCookie basicClientCookie = new HttpCookie(cookie[0], cookie[1]);
                                basicClientCookie.setDomain("." + URLUtils.getLeetcodeHost());
                                basicClientCookie.setPath("/");
                                cookieList.add(basicClientCookie);
                            } catch (IllegalArgumentException ignore) {

                            }
                        }
                    }
                    HttpRequestUtils.setCookie(cookieList);

                    ProgressManager.getInstance().run(new Task.Backgroundable(project, PluginConstant.ACTION_PREFIX + ".loginSuccess", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            if (HttpRequestUtils.isLogin(project)) {
                                HttpLogin.loginSuccess(project, cookieList);
                            } else {
                                JOptionPane.showMessageDialog(null, PropertiesUtils.getInfo("login.failed"));
                            }

                        }
                    });
                    super.doAction(e);
                }
            };
            okAction.putValue(Action.NAME, "login");
        }

        setModal(false);
        init();
        setTitle("login");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    @NotNull
    @Override
    protected Action getOKAction() {
        return okAction;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        Action helpAction = new AbstractAction("help") {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md");
            }

        };
        Action[] actions = new Action[]{helpAction, this.getOKAction(), this.getCancelAction()};
        return actions;
    }


    private static class JcefPanel extends JCEFHtmlPanel {


        private CefLoadHandlerAdapter cefLoadHandler;

        private Project project;

        private Action okAction;

        public JcefPanel(Project project, Action okAction, boolean old) {
            super( null);
            this.project = project;
            this.okAction = okAction;
            init();
        }

        public JcefPanel(Project project, Action okAction) {
            super(null, null);
            this.project = project;
            this.okAction = okAction;
            init();
        }

        private void init(){
            getJBCefClient().addLoadHandler(cefLoadHandler = new CefLoadHandlerAdapter() {

                boolean successDispose = false;

                @Override
                public void onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode, String errorText, String failedUrl) {
                    if (!successDispose) {
                        MessageUtils.getInstance(project).showWarnMsg("", "The page failed to load, please check the network and open it again");
                    }
                }

                @Override
                public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {

                    getJBCefCookieManager().getCefCookieManager().visitAllCookies(new CefCookieVisitor() {

                        private List<HttpCookie> cookieList = new ArrayList<>();

                        @Override
                        public boolean visit(CefCookie cefCookie, int count, int total, BoolRef boolRef) {

                            boolean isSession = Boolean.FALSE;
                            if (cefCookie.domain.contains("leetcode")) {
                                HttpCookie cookie = new HttpCookie(cefCookie.name, cefCookie.value);
                                cookie.setDomain(cefCookie.domain);
                                cookie.setPath(cefCookie.path);
                                cookieList.add(cookie);
                                if ("LEETCODE_SESSION".equals(cefCookie.name)) {
                                    isSession = Boolean.TRUE;
                                }
                            }
                            if (count == total - 1 && isSession) {
                                HttpRequestUtils.setCookie(cookieList);
                                if (HttpRequestUtils.isLogin(project)) {
                                    HttpLogin.loginSuccess(project, cookieList);
                                    MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("browser.login.success"));
                                    ApplicationManager.getApplication().invokeLater(() -> okAction.actionPerformed(null));
                                    successDispose = true;
                                } else {
                                    cookieList.clear();
                                    LogUtils.LOG.info("login failure");
                                }
                            }
                            return true;
                        }
                    });
                }
            }, getCefBrowser());
            loadURL(URLUtils.getLeetcodeLogin());
        }

        @Override
        public void dispose() {
            getJBCefClient().removeLoadHandler(cefLoadHandler, getCefBrowser());
            getJBCefBrowser(getCefBrowser()).getJBCefCookieManager().deleteCookies(URLUtils.leetcode, false);
            getJBCefBrowser(getCefBrowser()).getJBCefCookieManager().deleteCookies(URLUtils.leetcodecn, false);
            super.dispose();
        }
    }
}
