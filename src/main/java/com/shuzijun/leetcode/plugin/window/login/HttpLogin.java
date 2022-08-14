package com.shuzijun.leetcode.plugin.window.login;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.extension.NavigatorAction;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.HttpRequest;
import com.shuzijun.leetcode.platform.model.HttpResponse;
import com.shuzijun.leetcode.platform.model.User;
import com.shuzijun.leetcode.platform.service.HttpRequestService;
import com.shuzijun.leetcode.platform.utils.CookieUtils;
import com.shuzijun.leetcode.platform.utils.IOUtils;
import com.shuzijun.leetcode.platform.utils.LogUtils;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.PluginTopic;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.util.List;

/**
 * @author shuzijun
 */
public class HttpLogin {
    public static boolean ajaxLogin(Config config, NavigatorAction navigatorAction, Project project) {

        if (!URLUtils.isCn()) {
            return Boolean.FALSE;
        }

        if (StringUtils.isBlank(PersistentConfig.getInstance().getPassword(config.getLoginName()))) {
            return Boolean.FALSE;
        }
        HttpRequestService httpRequestService = RepositoryServiceImpl.getInstance(project).getHttpRequestService();
        try {
            HttpEntity ent = MultipartEntityBuilder.create()
                    .addTextBody("csrfmiddlewaretoken", httpRequestService.getToken() == null ? "" : httpRequestService.getToken())
                    .addTextBody("login", config.getLoginName())
                    .addTextBody("password", PersistentConfig.getInstance().getPassword(config.getLoginName()))
                    .addTextBody("next", "/problems")
                    .build();
            HttpResponse response = HttpRequest.builder(httpRequestService).post(URLUtils.getLeetcodeLogin(), ent.getContentType().getValue())
                    .body(IOUtils.toString(ent.getContent(), "UTF-8"))
                    .addHeader("x-requested-with", "XMLHttpRequest")
                    .addHeader("accept", "*/*").request();

            String body = response.getBody();

            if ((response.getStatusCode() == 200 || response.getStatusCode() == 302)) {
                if (StringUtils.isNotBlank(body) && body.startsWith("{")) {
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    JSONArray jsonArray = jsonObject.getJSONObject("form").getJSONArray("errors");
                    if (jsonArray.isEmpty()) {
                        MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                        NavigatorTabsPanel.loadUser(true, project);
                        ApplicationManager.getApplication().getMessageBus().syncPublisher(PluginTopic.LOGIN_TOPIC).login(project, config.getUrl());
                        examineEmail(project);
                        return Boolean.TRUE;
                    } else {
                        MessageUtils.getInstance(project).showInfoMsg("info", StringUtils.join(jsonArray, ","));
                        return Boolean.FALSE;
                    }
                } else if (StringUtils.isBlank(body)) {
                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                    NavigatorTabsPanel.loadUser(true, project);
                    ApplicationManager.getApplication().getMessageBus().syncPublisher(PluginTopic.LOGIN_TOPIC).login(project, config.getUrl());
                    examineEmail(project);
                    return Boolean.TRUE;
                } else {
                    httpRequestService.resetHttpclient();
                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.unknown"));
                    return Boolean.FALSE;
                }
            } else if (response.getStatusCode() == 400) {
                LogUtils.LOG.info("login 400:" + body);
                try {
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    MessageUtils.getInstance(project).showInfoMsg("info", StringUtils.join(jsonObject.getJSONObject("form").getJSONArray("errors"), ","));
                } catch (Exception ignore) {

                }
                return Boolean.FALSE;
            } else {
                httpRequestService.resetHttpclient();
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.unknown"));
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
                try {
                    User user = WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser();
                    if (user.isVerified() || user.isPhoneVerified()) {
                        return;
                    }
                    MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("user.email"));
                } catch (Exception i) {
                    LogUtils.LOG.error("验证邮箱错误");
                }
            }
        });
    }

    public static void loginSuccess(Project project, List<HttpCookie> cookieList) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, PluginConstant.ACTION_PREFIX + ".loginSuccess", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                Config config = PersistentConfig.getInstance().getInitConfig();
                config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.httpCookieToJSONString(cookieList));
                PersistentConfig.getInstance().setInitConfig(config);
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                NavigatorTabsPanel.loadUser(true, project);
                ApplicationManager.getApplication().getMessageBus().syncPublisher(PluginTopic.LOGIN_TOPIC).login(project, config.getUrl());
                examineEmail(project);
            }
        });
    }

    public static boolean isEnabledJcef() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        return config != null && !config.isCookie() && isSupportedJcef();
    }

    public static boolean isSupportedJcef() {
        try {
            Class<?> JBCefAppClass = Class.forName("com.intellij.ui.jcef.JBCefApp");
            Method method = JBCefAppClass.getMethod("isSupported");
            return (boolean) method.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            return Boolean.FALSE;
        }
    }

}
