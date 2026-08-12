package com.shuzijun.leetcode.plugin.window.login;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang3.StringUtils;
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
                jcefPanel = new JcefPanel(project, okAction, true);
            }
            Disposer.register(getDisposable(),jcefPanel);
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
                    setOKActionEnabled(false);
                    HttpLogin.cookieLoginAsync(project, cookiesString).whenComplete((loggedIn, throwable) ->
                            ApplicationManager.getApplication().invokeLater(() -> {
                                if (isDisposed()) {
                                    return;
                                }
                                setOKActionEnabled(true);
                                if (throwable != null) {
                                    LogUtils.LOG.warn("Failed to log in with cookies", throwable);
                                    MessageUtils.getInstance(project).showInfoMsg(
                                            "info",
                                            PropertiesUtils.getInfo("login.failed")
                                    );
                                    return;
                                }
                                if (Boolean.TRUE.equals(loggedIn)) {
                                    close(OK_EXIT_CODE);
                                }
                            }, ignored -> isDisposed())
                    );
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
                BrowserUtils.browse("https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md");
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
                    if (!successDispose && frame.isMain() && errorCode != CefLoadHandler.ErrorCode.ERR_ABORTED) {
                        LogUtils.LOG.warn("Failed to load LeetCode login page: " + failedUrl + ", " + errorCode + ", " + errorText);
                        MessageUtils.getInstance(project).showWarnMsg("", "The page failed to load, please check the network and open it again");
                    }
                }

                @Override
                public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {

                    if (isLoading || successDispose) {
                        return;
                    }

                    getJBCefCookieManager().getCefCookieManager().visitAllCookies(new CefCookieVisitor() {

                        private List<HttpCookie> cookieList = new ArrayList<>();

                        @Override
                        public boolean visit(CefCookie cefCookie, int count, int total, BoolRef boolRef) {

                            if (cefCookie.domain.contains("leetcode")) {
                                HttpCookie cookie = new HttpCookie(cefCookie.name, cefCookie.value);
                                cookie.setDomain(cefCookie.domain);
                                cookie.setPath(cefCookie.path);
                                cookieList.add(cookie);
                            }
                            if (count == total - 1) {
                                if (cookieList.stream().anyMatch(cookie -> cookie.getName().equals("LEETCODE_SESSION"))) {
                                    boolean loggedIn = false;
                                    try {
                                        LeetCodeServices.login().setCookies(cookieList);
                                        loggedIn = LeetCodeServices.login().isLoggedIn();
                                    } catch (Exception exception) {
                                        LogUtils.LOG.warn("Failed to import browser login cookies", exception);
                                    }
                                    if (loggedIn) {
                                        HttpLogin.loginSuccess(project, cookieList);
                                        MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("browser.login.success"));
                                        ApplicationManager.getApplication().invokeLater(() -> okAction.actionPerformed(null));
                                        successDispose = true;
                                    } else {
                                        cookieList.clear();
                                        try {
                                            LeetCodeServices.login().clearCookies();
                                        } catch (Exception exception) {
                                            LogUtils.LOG.warn("Failed to clear rejected browser login cookies", exception);
                                        }
                                        LogUtils.LOG.info("login failure");
                                    }
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
