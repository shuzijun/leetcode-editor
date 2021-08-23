package com.shuzijun.leetcode.plugin.window;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.util.List;

/**
 * @author shuzijun
 */
public class HttpLogin {
    public static boolean ajaxLogin(Config config, JTree tree, Project project) {

        if (URLUtils.leetcode.equals(URLUtils.getLeetcodeHost())) {
            return Boolean.FALSE;
        }

        if (StringUtils.isBlank(PersistentConfig.getInstance().getPassword(config.getLoginName()))) {
            return Boolean.FALSE;
        }

        try {
            HttpEntity ent = MultipartEntityBuilder.create()
                    .addTextBody("csrfmiddlewaretoken", HttpRequestUtils.getToken() == null ? "": HttpRequestUtils.getToken())
                    .addTextBody("login", config.getLoginName())
                    .addTextBody("password", PersistentConfig.getInstance().getPassword(config.getLoginName()))
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
                LogUtils.LOG.error("login 400:" + body);
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

    public static void loginSuccess(JTree tree, Project project, List<HttpCookie> cookieList) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, PluginConstant.ACTION_PREFIX+".loginSuccess", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                Config config = PersistentConfig.getInstance().getInitConfig();
                config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.httpCookieToJSONString(cookieList));
                PersistentConfig.getInstance().setInitConfig(config);
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                ViewManager.loadServiceData(tree, project);
                examineEmail(project);
            }
        });
    }

    public static boolean isEnabledJcef() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        return config != null && config.getJcef() && isSupportedJcef();
    }

    public static boolean isSupportedJcef() {
        try {
            Class<?> JBCefAppClass = Class.forName("com.intellij.ui.jcef.JBCefApp");
            Method method = JBCefAppClass.getMethod("isSupported");
            return (boolean) method.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return Boolean.FALSE;
        }
    }

}
