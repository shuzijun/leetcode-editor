package com.shuzijun.leetcode.plugin.window;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCookieVisitor;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginFrame {

    private JTree tree;
    private Project project;


    public LoginFrame(Project project, JTree tree) {
        this.tree = tree;
        this.project = project;
    }

    public void loadComponent() {
        Window activeFrame = IdeFrameImpl.getActiveFrame();
        if (activeFrame == null) {
            return;
        }
        Rectangle bounds = activeFrame.getGraphicsConfiguration().getBounds();
        final JFrame frame = new IdeFrameImpl();
        frame.setDefaultCloseOperation(2);
        frame.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
        frame.setLayout(new BorderLayout());

        if (classLoader()) {
            loadJCEFComponent(frame);
        } else {
            loadCookieComponent(frame);
        }
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
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
                if(!successDispose) {
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
                                loginSuccess(tree, project, cookieList);
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

    private void loadCookieComponent(JFrame frame) {

        JTextArea cookieText = new JTextArea();
        cookieText.setLineWrap(true);
        cookieText.setMinimumSize(new Dimension(400, 200));
        cookieText.setPreferredSize(new Dimension(400, 200));

        frame.add(cookieText, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton cookieHelp = new JButton("help");
        cookieHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md");
            }
        });

        JButton loginButton = new JButton("login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                ProgressManager.getInstance().run(new Task.Backgroundable(project, "leetcode.editor.login", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        if (HttpRequestUtils.isLogin()) {
                            loginSuccess(tree, project, cookieList);
                            return;
                        } else {
                            JOptionPane.showMessageDialog(null, PropertiesUtils.getInfo("login.failed"));
                            return;
                        }
                    }
                });
                frame.dispose();
            }
        });

        JButton closeButton = new JButton("close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(cookieHelp);
        buttonPanel.add(closeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

    }

    private boolean classLoader() {
        synchronized (this) {
            try {
                Config config = PersistentConfig.getInstance().getInitConfig();
                return config.getJcef() && JBCefApp.isSupported();
            } catch (Throwable e) {
                return Boolean.FALSE;
            }

        }
    }

    private void loginSuccess(JTree tree, Project project, List<HttpCookie> cookieList) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "leetcode.loginSuccess", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                Config config = PersistentConfig.getInstance().getInitConfig();
                config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.httpCookieToJSONString(cookieList));
                PersistentConfig.getInstance().setInitConfig(config);
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                ViewManager.loadServiceData(tree, project);
                httpLogin.examineEmail(project);
            }
        });
    }

    public static class httpLogin {
        public static boolean ajaxLogin(Config config, JTree tree, Project project) {
            if (StringUtils.isBlank(PersistentConfig.getInstance().getPassword())) {
                return Boolean.FALSE;
            }

            try {
                HttpEntity ent = MultipartEntityBuilder.create()
                        .addTextBody("csrfmiddlewaretoken", HttpRequestUtils.getToken())
                        .addTextBody("login", config.getLoginName())
                        .addTextBody("password", PersistentConfig.getInstance().getPassword())
                        .addTextBody("next", "/problems")
                        .build();
                HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeLogin(), ent.getContentType().getValue());
                httpRequest.setBody(IOUtils.toString(ent.getContent(), "UTF-8"));
                httpRequest.addHeader("x-requested-with", "XMLHttpRequest");
                httpRequest.addHeader("accept", "*/*");
                HttpResponse response = HttpRequestUtils.executePost(httpRequest);

                if (response == null) {
                    MessageUtils.getInstance(project).showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                    return Boolean.FALSE;
                }

                String body = response.getBody();

                if ((response.getStatusCode() == 200 || response.getStatusCode() == 302)) {
                    if (StringUtils.isNotBlank(body) && body.startsWith("{")) {
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        JSONArray jsonArray = jsonObject.getJSONObject("form").getJSONArray("errors");
                        if (jsonArray.isEmpty()) {
                            MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                            examineEmail(project);
                            ViewManager.loadServiceData(tree, project);
                            return Boolean.TRUE;
                        } else {
                            MessageUtils.getInstance(project).showInfoMsg("info", StringUtils.join(jsonArray, ","));
                            return Boolean.FALSE;
                        }
                    } else if (StringUtils.isBlank(body)) {
                        MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                        examineEmail(project);
                        ViewManager.loadServiceData(tree, project);
                        return Boolean.TRUE;
                    } else {
                        HttpRequestUtils.resetHttpclient();
                        MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.unknown"));
                        SentryUtils.submitErrorReport(null, String.format("login.unknown:\nStatusCode:%s\nbody:%s", response.getStatusCode(), body));
                        return Boolean.FALSE;
                    }
                } else if (response.getStatusCode() == 400) {
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    MessageUtils.getInstance(project).showInfoMsg("info", StringUtils.join(jsonObject.getJSONObject("form").getJSONArray("errors"), ","));
                    return Boolean.FALSE;
                } else {
                    HttpRequestUtils.resetHttpclient();
                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.unknown"));
                    SentryUtils.submitErrorReport(null, String.format("login.unknown:\nStatusCode:%s\nbody:%s", response.getStatusCode(), body));
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                LogUtils.LOG.error("登陆错误", e);
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.failed"));
                return Boolean.FALSE;
            }
        }

        public static void examineEmail(Project project) {
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                @Override
                public void run() {
                    HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
                    try {
                        httpRequest.setBody("{\"operationName\":\"user\",\"variables\":{},\"query\":\"query user {\\n  user {\\n    socialAccounts\\n    username\\n    emails {\\n      email\\n      primary\\n      verified\\n      __typename\\n    }\\n    phone\\n    profile {\\n      rewardStats\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
                        httpRequest.addHeader("Accept", "application/json");
                        HttpResponse response = HttpRequestUtils.executePost(httpRequest);
                        if (response != null && response.getStatusCode() == 200) {

                            String body = response.getBody();

                            JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("user").getJSONArray("emails");
                            if (jsonArray != null && jsonArray.size() > 0) {
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    if (object.getBoolean("verified")) {
                                        return;
                                    }
                                }

                            }
                            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("user.email"));
                        }
                    } catch (Exception i) {
                        LogUtils.LOG.error("验证邮箱错误");
                    }
                }
            });
        }
    }
}
