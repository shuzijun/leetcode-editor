package com.shuzijun.leetcode.plugin.window.login;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.lc.command.LoginCommand;
import com.shuzijun.leetcode.plugin.application.CacheInvalidationCoordinator;
import com.shuzijun.leetcode.plugin.application.CacheInvalidationReason;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.application.LoginGenerationTracker;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        try {
            LoginCommand.LoginResult result = LeetCodeServices.login().login(
                    config.getLoginName(),
                    PersistentConfig.getInstance().getPassword(config.getLoginName()),
                    LeetCodeServices.login().csrfToken()
            );
            if (result.isSuccess()) {
                MessageUtils.getInstance(project).showInfoMsg(
                        "info",
                        PropertiesUtils.getInfo("login.success")
                );
                notifyLoginAfterUserLoaded(project, config.getUrl());
                examineEmail(project);
                return Boolean.TRUE;
            }
            if (!result.getErrors().isEmpty()) {
                MessageUtils.getInstance(project).showInfoMsg(
                        "info",
                        StringUtils.join(result.getErrors(), ",")
                );
                return Boolean.FALSE;
            }
            if (result.getStatusCode() == 400) {
                LogUtils.LOG.info("login 400:" + result.getResponseBody());
            }
            LeetCodeServices.login().clearCookies();
            MessageUtils.getInstance(project).showInfoMsg(
                    "info",
                    PropertiesUtils.getInfo("login.unknown")
            );
            return Boolean.FALSE;
        } catch (Exception e) {
            LogUtils.LOG.error("登陆错误", e);
            MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.failed"));
            return Boolean.FALSE;
        }
    }

    public static void cookieLogin(Project project, String cookies) {
        ProductServices.cookieLoginStrategy().login(project, cookies);
    }

    public static CompletableFuture<Boolean> cookieLoginAsync(Project project, String cookies) {
        return ProductServices.cookieLoginStrategy().loginAsync(project, cookies);
    }

    public static void examineEmail(Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = QuestionManager.getUser();
                    if (!shouldWarnUnverifiedUser(user)) {
                        return;
                    }
                    MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("user.email"));
                } catch (Exception i) {
                    LogUtils.LOG.error("Failed to check email verification status", i);
                }
            }
        });
    }

    static boolean shouldWarnUnverifiedUser(User user) {
        return user != null && user.isSignedIn() && !user.isVerified() && !user.isPhoneVerified();
    }

    public static void loginSuccess(Project project, List<HttpCookie> cookieList) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, PluginConstant.ACTION_PREFIX + ".loginSuccess", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                Config config = PersistentConfig.getInstance().getInitConfig();
                config.addCookie(config.getUrl() + config.getLoginName(), CookieUtils.httpCookieToJSONString(cookieList));
                PersistentConfig.getInstance().setInitConfig(config);
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
                notifyLoginAfterUserLoaded(project, config.getUrl());
                examineEmail(project);
            }
        });
    }

    public static void notifyLoginAfterUserLoaded(Project project, String host) {
        CacheInvalidationCoordinator.invalidate(
                CacheInvalidationReason.LOGIN,
                host,
                endpoint(host)
        );
        long generation = LoginGenerationTracker.next();
        NavigatorTabsPanel.loadUser(true).whenComplete((user, throwable) -> {
            if (throwable != null) {
                LogUtils.LOG.warn("Failed to synchronize user data after login", throwable);
                return;
            }
            if (user != null
                    && user.isSignedIn()
                    && !project.isDisposed()
                    && LoginGenerationTracker.isCurrent(generation)) {
                ApplicationManager.getApplication().getMessageBus().syncPublisher(LoginNotifier.TOPIC).login(project, host);
            }
        });
    }

    private static String endpoint(String host) {
        return "https://" + host;
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
