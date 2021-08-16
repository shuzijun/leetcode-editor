package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.HttpRequestUtils;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
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
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class JcefLogin implements LoginFrame {

    private JTree tree;
    private Project project;


    public JcefLogin(Project project, JTree tree) {
        this.tree = tree;
        this.project = project;
    }

    @Override
    public void loadComponent() {
        CookiePanel cookiePanel = new CookiePanel(project);
        cookiePanel.show();
    }


   private class CookiePanel extends DialogWrapper {

        private JPanel jpanel;
        private LoginJCEFPanel loginJCEFPanel;

        public CookiePanel(Project project) {
            super(project, Boolean.TRUE);

            jpanel = new JBPanel();
            jpanel.setLayout(new BorderLayout());
            loginJCEFPanel = new LoginJCEFPanel();
            loginJCEFPanel.getComponent().setMinimumSize(new Dimension(1000, 500));
            loginJCEFPanel.getComponent().setPreferredSize(new Dimension(1000, 500));
            jpanel.add(new JBScrollPane(loginJCEFPanel.getComponent(), JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
            setModal(true);
            init();
            setTitle("login");
            loginJCEFPanel.loadURL(URLUtils.getLeetcodeLogin());
        }

        @NotNull
        @Override
        protected Action getOKAction() {

            return null;
        }

        @NotNull
        @Override
        protected Action[] createActions() {
            return new Action[]{};
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }

       @Override
       protected void dispose() {
           Disposer.dispose(loginJCEFPanel);
           super.dispose();
       }
   }


    private  class LoginJCEFPanel extends JCEFHtmlPanel {

        private CefLoadHandlerAdapter cefLoadHandler;

        public LoginJCEFPanel() {
            super("about:blank");
            getJBCefClient().addLoadHandler(cefLoadHandler = new CefLoadHandlerAdapter() {

                boolean successDispose = false;

                @Override
                public void onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode, String errorText, String failedUrl) {
                    if (!successDispose) {
                        browser.executeJavaScript("alert('The page failed to load, please check the network and open it again')", PluginConstant.PLUGIN_ID, 0);
                    }
                }

                @Override
                public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {

                    getJBCefBrowser(getCefBrowser()).getJBCefCookieManager().getCefCookieManager().visitAllCookies(new CefCookieVisitor() {

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
                                if (HttpRequestUtils.isLogin()) {
                                    HttpLogin.loginSuccess(tree, project, cookieList);
                                    browser.executeJavaScript("alert('Login is successful. close the window')", "leetcode-editor", 0);
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
        }

        @Override
        public void dispose() {
            getJBCefClient().removeLoadHandler(cefLoadHandler,getCefBrowser());
            getJBCefBrowser(getCefBrowser()).getJBCefCookieManager().deleteCookies(URLUtils.leetcode, false);
            getJBCefBrowser(getCefBrowser()).getJBCefCookieManager().deleteCookies(URLUtils.leetcodecn, false);
            super.dispose();
        }
    }

}
