package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public final class CacheInvalidationCoordinator {

    private CacheInvalidationCoordinator() {
    }

    public static void invalidate(@NotNull CacheInvalidationReason reason) {
        String host = URLUtils.getLeetcodeHost();
        String endpoint = URLUtils.getLeetcodeUrl();
        if (reason.invalidatesApi()) {
            LeetCodeApiService.invalidateCaches(host);
        }
        if (reason.invalidatesClient()) {
            LcClientFactory.invalidateEndpoint(endpoint);
        }
    }

    public static void invalidate(
            @NotNull CacheInvalidationReason reason,
            @NotNull String host,
            @NotNull String endpoint
    ) {
        if (reason.invalidatesApi() && StringUtils.isNotBlank(host)) {
            LeetCodeApiService.invalidateCaches(host);
        }
        if (reason.invalidatesClient() && StringUtils.isNotBlank(endpoint)) {
            LcClientFactory.invalidateEndpoint(endpoint);
        }
    }

    public static void invalidateSiteChange(
            String oldHost,
            String oldEndpoint,
            String newHost,
            String newEndpoint
    ) {
        invalidate(CacheInvalidationReason.SITE_CHANGE, oldHost, oldEndpoint);
        if (!StringUtils.equals(oldHost, newHost)
                || !StringUtils.equals(oldEndpoint, newEndpoint)) {
            invalidate(CacheInvalidationReason.SITE_CHANGE, newHost, newEndpoint);
        }
    }
}
