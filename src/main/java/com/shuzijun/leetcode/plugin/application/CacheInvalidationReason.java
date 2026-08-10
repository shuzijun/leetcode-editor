package com.shuzijun.leetcode.plugin.application;

public enum CacheInvalidationReason {
    REFRESH(true, false),
    LOGIN(true, true),
    LOGOUT(true, true),
    ACCOUNT_CHANGE(true, true),
    SITE_CHANGE(true, true),
    LANGUAGE_CHANGE(true, false),
    TEMPLATE_CHANGE(false, false);

    private final boolean invalidateApi;
    private final boolean invalidateClient;

    CacheInvalidationReason(boolean invalidateApi, boolean invalidateClient) {
        this.invalidateApi = invalidateApi;
        this.invalidateClient = invalidateClient;
    }

    boolean invalidatesApi() {
        return invalidateApi;
    }

    boolean invalidatesClient() {
        return invalidateClient;
    }
}
