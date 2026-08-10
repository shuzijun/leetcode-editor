package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.spi.CookieLoginStrategy;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.login.HttpLogin;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DefaultCookieLoginStrategy implements CookieLoginStrategy {

    @Override
    public void login(@NotNull Project project, @NotNull String cookies) {
        loginAsync(project, cookies);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> loginAsync(
            @NotNull Project project,
            @NotNull String cookies
    ) {
        return CompletableFuture.supplyAsync(
                () -> login(project, parseCookies(cookies)),
                AppExecutorUtil.getAppExecutorService()
        );
    }

    static List<HttpCookie> parseCookies(String cookies) {
        List<HttpCookie> cookieList = new ArrayList<>();
        for (String cookieString : cookies.split(";")) {
            String[] cookie = cookieString.trim().split("=", 2);
            if (cookie.length != 2 || cookie[0].isEmpty()) {
                continue;
            }
            try {
                HttpCookie parsedCookie = new HttpCookie(cookie[0], cookie[1]);
                parsedCookie.setDomain("." + URLUtils.getLeetcodeHost());
                parsedCookie.setPath("/");
                cookieList.add(parsedCookie);
            } catch (IllegalArgumentException exception) {
                LogUtils.LOG.debug("Ignoring an invalid login cookie", exception);
            }
        }
        return cookieList;
    }

    private boolean login(Project project, List<HttpCookie> cookies) {
        try {
            LeetCodeServices.login().setCookies(cookies);
            if (LeetCodeServices.login().isLoggedIn()) {
                HttpLogin.loginSuccess(project, cookies);
                return true;
            }
        } catch (Exception exception) {
            LogUtils.LOG.warn("Failed to log in with cookies", exception);
        }
        try {
            LeetCodeServices.login().clearCookies();
        } catch (Exception exception) {
            LogUtils.LOG.warn("Failed to clear rejected login cookies", exception);
        }
        MessageUtils.getInstance(project).showInfoMsg(
                "info",
                PropertiesUtils.getInfo("login.failed")
        );
        return false;
    }
}
