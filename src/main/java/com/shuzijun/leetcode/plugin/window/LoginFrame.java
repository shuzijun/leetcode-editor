package com.shuzijun.leetcode.plugin.window;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codebrig.journey.JourneyBrowserView;
import com.codebrig.journey.JourneyLoader;
import com.codebrig.journey.proxy.CefBrowserProxy;
import com.codebrig.journey.proxy.browser.CefFrameProxy;
import com.codebrig.journey.proxy.callback.CefCookieVisitorProxy;
import com.codebrig.journey.proxy.handler.CefLoadHandlerProxy;
import com.codebrig.journey.proxy.misc.BoolRefProxy;
import com.codebrig.journey.proxy.network.CefCookieManagerProxy;
import com.codebrig.journey.proxy.network.CefCookieProxy;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.FrameWrapper;
import com.intellij.ui.components.JBPanel;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.joor.Reflect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Proxy;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginFrame extends FrameWrapper {

    private JTree tree;
    private Project project;

    private boolean isComponent = false;

    public LoginFrame(Project project, JTree tree) {
        super(project);
        this.tree = tree;
        this.project = project;
    }

    public void loadComponent() {
        isComponent = true;
        if (classLoader()) {
            loadJCEFComponent();

            setTitle("login");
        } else {
            loadCookieComponent();
            setSize(new Dimension(400, 200));

            setTitle("login cookie");
        }
    }

    private void loadJCEFComponent() {

        JourneyBrowserView browser = new JourneyBrowserView(URLUtils.getLeetcodeLogin());

        this.getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                browser.getCefClient().dispose();
            }
        });
        browser.getCefClient().addLoadHandler(CefLoadHandlerProxy.createHandler(new CefLoadHandlerProxy() {

            @Override
            public void onLoadingStateChange(CefBrowserProxy cefBrowserProxy, boolean b, boolean b1, boolean b2) {
                LogUtils.LOG.debug("onLoadEnd:" + cefBrowserProxy.getURL());
                if (CefCookieManagerProxy.getGlobalManager() != null) {
                    final List<HttpCookie> cookieList = new ArrayList<>();
                    final Boolean[] isSession = {Boolean.FALSE};

                    boolean v = CefCookieManagerProxy.getGlobalManager().visitAllCookies(CefCookieVisitorProxy.createVisitor(new CefCookieVisitorProxy() {

                        @Override
                        public boolean visit(CefCookieProxy cefCookieProxy, int count, int total, BoolRefProxy boolRefProxy) {
                            Object cefCookie = ((Reflect.ProxyInvocationHandler) Proxy.getInvocationHandler(cefCookieProxy)).getUnderlyingObject();
                            if (Reflect.on(cefCookie).field("domain").toString().contains("leetcode")) {
                                HttpCookie cookie = new HttpCookie(Reflect.on(cefCookie).field("name").toString(), Reflect.on(cefCookie).field("value").toString());
                                cookie.setDomain(Reflect.on(cefCookie).field("domain").toString());
                                cookie.setPath(Reflect.on(cefCookie).field("path").toString());
                                cookieList.add(cookie);
                                if ("LEETCODE_SESSION".equals(Reflect.on(cefCookie).field("name").toString())) {
                                    isSession[0] = Boolean.TRUE;
                                }
                            }
                            if (count == total - 1 && isSession[0]) {
                                HttpRequestUtils.setCookie(cookieList);
                                if (HttpRequestUtils.isLogin()) {
                                    Config config = PersistentConfig.getInstance().getInitConfig();
                                    config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.httpCookieToJSONString(cookieList));
                                    PersistentConfig.getInstance().setInitConfig(config);
                                    ViewManager.loadServiceData(tree, project);
                                    httpLogin.examineEmail(project);
                                    cefBrowserProxy.executeJavaScript("alert('Login is successful. Please close the window')", "leetcode-editor", 0);
                                } else {
                                    LogUtils.LOG.info("login failure");
                                }
                            }
                            return true;
                        }
                    }));

                }
            }

            @Override
            public void onLoadEnd(CefBrowserProxy cefBrowserProxy, CefFrameProxy cefFrameProxy, int i) {

            }

            @Override
            public void onLoadError(CefBrowserProxy cefBrowserProxy, CefFrameProxy cefFrameProxy, CefLoadHandlerProxy.ErrorCode errorCode, String s, String s1) {
                cefFrameProxy.executeJavaScript("alert('The page failed to load, please check the network and open it again')", "leetcode-editor", 0);
            }
        }));

        this.setComponent(browser);
    }

    private void loadCookieComponent() {

        JBPanel panel = new JBPanel();
        panel.setLayout(new BorderLayout());

        JTextArea cookieText = new JTextArea();
        cookieText.setLineWrap(true);
        cookieText.setMinimumSize(new Dimension(400, 200));
        cookieText.setPreferredSize(new Dimension(400, 200));

        panel.add(cookieText, BorderLayout.CENTER);

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
                        HttpCookie basicClientCookie = new HttpCookie(cookie[0], cookie[1]);
                        basicClientCookie.setDomain("." + URLUtils.getLeetcodeHost());
                        basicClientCookie.setPath("/");
                        cookieList.add(basicClientCookie);
                    }
                }
                HttpRequestUtils.setCookie(cookieList);
                if (HttpRequestUtils.isLogin()) {
                    Config config = PersistentConfig.getInstance().getInitConfig();
                    config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.httpCookieToJSONString(cookieList));
                    PersistentConfig.getInstance().setInitConfig(config);
                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                    ViewManager.loadServiceData(tree, project);
                    httpLogin.examineEmail(project);
                    close();
                    return;
                } else {
                    JOptionPane.showMessageDialog(null, PropertiesUtils.getInfo("login.failed"));
                    return;
                }
            }
        });

        JButton closeButton = new JButton("close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(cookieHelp);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        this.setComponent(panel);

    }

    private boolean classLoader() {
        synchronized (this) {
            String path = PathManager.getPluginsPath() + File.separator + "leetcode-editor" + File.separator + "natives" + File.separator;
            if (!new File(path, "icudtl.dat").exists()
                    && !new File(path, "jcef_app.app").exists()) {
                return Boolean.FALSE;
            } else {
                JourneyLoader.getJourneyClassLoader(path);
                return Boolean.TRUE;
            }
        }
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
                HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeLogin(),ent.getContentType().getValue());
                httpRequest.setBody(IOUtils.toString(ent.getContent(),"UTF-8"));
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
                    HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
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
