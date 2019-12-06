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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import org.joor.Reflect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginPanel extends DialogWrapper {

    private JTree tree;

    public LoginPanel(@Nullable Project project, JTree tree) {
        super(project, true);
        this.tree = tree;
        classLoader();
        setModal(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        JPanel dialogPanel = new JPanel(new BorderLayout());

        JourneyBrowserView browser = new JourneyBrowserView(URLUtils.getLeetcodeLogin());

        Window frame = getWindow();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (browser != null) {
                    browser.getCefClient().dispose();
                }
                frame.dispose();
            }
        });


        browser.getCefClient().addLoadHandler(CefLoadHandlerProxy.createHandler(new CefLoadHandlerProxy() {

            @Override
            public void onLoadingStateChange(CefBrowserProxy cefBrowserProxy, boolean b, boolean b1, boolean b2) {
                LogUtils.LOG.debug("onLoadEnd:" + cefBrowserProxy.getURL());
                if (CefCookieManagerProxy.getGlobalManager() != null) {
                    final List<BasicClientCookie> cookieList = new ArrayList<>();
                    final Boolean[] isSession = {Boolean.FALSE};

                    boolean v = CefCookieManagerProxy.getGlobalManager().visitAllCookies(CefCookieVisitorProxy.createVisitor(new CefCookieVisitorProxy() {

                        @Override
                        public boolean visit(CefCookieProxy cefCookieProxy, int count, int total, BoolRefProxy boolRefProxy) {
                            Object cefCookie = ((Reflect.ProxyInvocationHandler) Proxy.getInvocationHandler(cefCookieProxy)).getUnderlyingObject();
                            if (Reflect.on(cefCookie).field("domain").toString().contains("leetcode")) {
                                BasicClientCookie cookie = new BasicClientCookie(Reflect.on(cefCookie).field("name").toString(), Reflect.on(cefCookie).field("value").toString());
                                cookie.setDomain(Reflect.on(cefCookie).field("domain").toString());
                                cookie.setPath(Reflect.on(cefCookie).field("path").toString());
                                cookieList.add(cookie);
                                if ("LEETCODE_SESSION".equals(Reflect.on(cefCookie).field("name").toString())) {
                                    isSession[0] = Boolean.TRUE;
                                }
                            }
                            if (count == total - 1 && isSession[0]) {
                                HttpClientUtils.setCookie(cookieList);
                                if (HttpClientUtils.isLogin()) {
                                    Config config = PersistentConfig.getInstance().getInitConfig();
                                    config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.toJSONString(cookieList));
                                    PersistentConfig.getInstance().setInitConfig(config);
                                    ViewManager.loadServiceData(tree);
                                    examineEmail();
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
            public void onLoadError(CefBrowserProxy cefBrowserProxy, CefFrameProxy cefFrameProxy, ErrorCode errorCode, String s, String s1) {
                cefFrameProxy.executeJavaScript("alert('The page failed to load, please check the network and open it again')", "leetcode-editor", 0);
            }
        }));

        dialogPanel.setVisible(true);
        dialogPanel.setPreferredSize(new Dimension(600, 400));

        dialogPanel.add(browser);

        return dialogPanel;
    }

    private void classLoader() {
        synchronized (this) {
            String path = PathManager.getPluginsPath() + File.separator + "leetcode-editor" + File.separator + "natives" + File.separator;
            if (!new File(path, "icudtl.dat").exists()
                    && !new File(path, "jcef_app.app").exists()) {
                MessageUtils.showErrorMsg("login err", "natives Path is empty,path:" + path);
                throw new RuntimeException("natives Path is empty,path:" + path);
            } else {
                JourneyLoader.getJourneyClassLoader(path);
            }
        }
    }


    @Override
    protected void dispose() {
        super.dispose();
    }


    private void examineEmail() {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
                try {
                    StringEntity entity = new StringEntity("{\"operationName\":\"user\",\"variables\":{},\"query\":\"query user {\\n  user {\\n    socialAccounts\\n    username\\n    emails {\\n      email\\n      primary\\n      verified\\n      __typename\\n    }\\n    phone\\n    profile {\\n      rewardStats\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
                    post.setEntity(entity);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");
                    CloseableHttpResponse response = HttpClientUtils.executePost(post);
                    if (response != null && response.getStatusLine().getStatusCode() == 200) {

                        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("user").getJSONArray("emails");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                if (object.getBoolean("verified")) {
                                    return;
                                }
                            }

                        }
                        MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("user.email"));
                    }
                } catch (IOException i) {
                    LogUtils.LOG.error("验证邮箱错误");
                } finally {
                    post.abort();
                }
            }
        });
    }
}
