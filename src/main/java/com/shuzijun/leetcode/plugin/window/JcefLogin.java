package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.jcef.JBCefBrowser;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
        Window activeFrame = IdeFrameImpl.getActiveFrame();
        if (activeFrame == null) {
            return;
        }
        Rectangle bounds = activeFrame.getGraphicsConfiguration().getBounds();
        final JFrame frame = new IdeFrameImpl();
        frame.setTitle("JCEF login");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(2);
        frame.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
        frame.setLayout(new BorderLayout());

        loadJCEFComponent(frame);

        frame.setVisible(true);

    }

    private void loadJCEFComponent(JFrame frame) {
        final JBCefBrowser jbCefBrowser = new JBCefBrowser(URLUtils.getLeetcodeLogin());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                jbCefBrowser.getJBCefCookieManager().deleteCookies(URLUtils.leetcode, false);
                jbCefBrowser.getJBCefCookieManager().deleteCookies(URLUtils.leetcodecn, false);
                Disposer.dispose(jbCefBrowser);
            }
        });


        jbCefBrowser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {

            boolean successDispose = false;

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode, String errorText, String failedUrl) {
                if (!successDispose) {
                    browser.executeJavaScript("alert('The page failed to load, please check the network and open it again')", "leetcode-editor", 0);
                }
            }

            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {

                jbCefBrowser.getJBCefCookieManager().getCefCookieManager().visitAllCookies(new CefCookieVisitor() {

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
                                //browser.executeJavaScript("alert('Login is successful. close the window')", "leetcode-editor", 0);
                                successDispose = true;
                                frame.dispose();
                            } else {
                                cookieList.clear();
                                LogUtils.LOG.info("login failure");
                            }
                        }
                        return true;
                    }
                });
            }
        }, jbCefBrowser.getCefBrowser());

        frame.add(jbCefBrowser.getComponent(), BorderLayout.CENTER);

    }

}
