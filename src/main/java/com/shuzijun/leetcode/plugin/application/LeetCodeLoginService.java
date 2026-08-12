package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.command.LoginCommand;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.Checkin;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.util.List;

public final class LeetCodeLoginService {

    @NotNull
    public LoginCommand.LoginResult login(
            @NotNull String username,
            @NotNull String password,
            String csrfToken
    ) throws LcException {
        return client().api().account().login(
                username,
                password,
                csrfToken,
                RequestContext.DEFAULT
        );
    }

    @NotNull
    public Checkin checkin() throws LcException {
        return client().api().account().checkin(RequestContext.DEFAULT);
    }

    public boolean verify() {
        try {
            return client().api().account().verify(RequestContext.DEFAULT);
        } catch (LcException exception) {
            LogUtils.LOG.warn("Failed to verify the LeetCode endpoint", exception);
            return false;
        }
    }

    public boolean isLoggedIn() {
        try {
            return client().api().account().isLoggedIn(RequestContext.DEFAULT);
        } catch (LcException exception) {
            LogUtils.LOG.warn("Failed to check the LeetCode login state", exception);
            return false;
        }
    }

    public void setCookies(@NotNull List<HttpCookie> cookies) throws LcException {
        client().api().account().setCookies(cookies, RequestContext.DEFAULT);
    }

    @NotNull
    public List<HttpCookie> cookies() throws LcException {
        return client().api().account().cookies(RequestContext.DEFAULT);
    }

    public String csrfToken() {
        try {
            return client().api().account().csrfToken(RequestContext.DEFAULT);
        } catch (LcException exception) {
            LogUtils.LOG.warn("Failed to read the LeetCode CSRF token", exception);
            return null;
        }
    }

    public void clearCookies() throws LcException {
        client().api().account().logout(RequestContext.DEFAULT);
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }
}
