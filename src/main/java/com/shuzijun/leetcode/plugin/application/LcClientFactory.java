package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.LcClientConfig;
import com.shuzijun.lc.LcEndpoint;
import com.shuzijun.lc.http.HttpClient;
import com.shuzijun.leetcode.plugin.utils.HttpRequestUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class LcClientFactory {

    private static final ConcurrentMap<ClientKey, LcClient> CLIENTS = new ConcurrentHashMap<>();

    private LcClientFactory() {
    }

    @NotNull
    static LcClient create() {
        HttpClient.SiteEnum site = URLUtils.isCn() ? HttpClient.SiteEnum.CN : HttpClient.SiteEnum.EN;
        return create(site, URLUtils.getLeetcodeUrl());
    }

    @NotNull
    static LcClient create(@NotNull HttpClient.SiteEnum site, @NotNull String endpoint) {
        return CLIENTS.computeIfAbsent(new ClientKey(site, endpoint), ignored ->
                LcClient.create(LcClientConfig.builder(LcEndpoint.custom(site, endpoint))
                        .executorHttp(HttpRequestUtils.getExecutorHttp())
                        .build())
        );
    }

    static void invalidateEndpoint(@NotNull String endpoint) {
        CLIENTS.keySet().removeIf(key -> key.endpoint.equals(endpoint));
    }

    static void invalidateSite(@NotNull HttpClient.SiteEnum site) {
        CLIENTS.keySet().removeIf(key -> key.site == site);
    }

    static void invalidateAll() {
        CLIENTS.clear();
    }

    private static final class ClientKey {
        private final HttpClient.SiteEnum site;
        private final String endpoint;

        private ClientKey(HttpClient.SiteEnum site, String endpoint) {
            this.site = site;
            this.endpoint = endpoint;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof ClientKey)) {
                return false;
            }
            ClientKey key = (ClientKey) object;
            return site == key.site && endpoint.equals(key.endpoint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(site, endpoint);
        }
    }
}
