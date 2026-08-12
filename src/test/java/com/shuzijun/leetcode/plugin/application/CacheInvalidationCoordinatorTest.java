package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.http.HttpClient;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class CacheInvalidationCoordinatorTest {

    @After
    public void clearClients() {
        LcClientFactory.invalidateAll();
        LeetCodeApiService.invalidateCaches();
    }

    @Test
    public void refreshLanguageAndTemplateChangesPreserveClientSession() {
        String endpoint = endpoint("content");
        LcClient client = LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint);

        CacheInvalidationCoordinator.invalidate(
                CacheInvalidationReason.REFRESH,
                "leetcode.com",
                endpoint
        );
        CacheInvalidationCoordinator.invalidate(
                CacheInvalidationReason.LANGUAGE_CHANGE,
                "leetcode.com",
                endpoint
        );
        CacheInvalidationCoordinator.invalidate(
                CacheInvalidationReason.TEMPLATE_CHANGE,
                "leetcode.com",
                endpoint
        );

        assertSame(client, LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint));
    }

    @Test
    public void accountLifecycleReasonsInvalidateOnlyRequestedEndpoint() {
        for (CacheInvalidationReason reason : new CacheInvalidationReason[]{
                CacheInvalidationReason.LOGIN,
                CacheInvalidationReason.LOGOUT,
                CacheInvalidationReason.ACCOUNT_CHANGE
        }) {
            String endpoint = endpoint(reason.name().toLowerCase());
            String otherEndpoint = endpoint(reason.name().toLowerCase() + "-other");
            LcClient client = LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint);
            LcClient other = LcClientFactory.create(HttpClient.SiteEnum.EN, otherEndpoint);

            CacheInvalidationCoordinator.invalidate(reason, "leetcode.com", endpoint);
            CacheInvalidationCoordinator.invalidate(reason, "leetcode.com", endpoint);

            assertNotSame(client, LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint));
            assertSame(other, LcClientFactory.create(HttpClient.SiteEnum.EN, otherEndpoint));
        }
    }

    @Test
    public void siteChangeInvalidatesOnlyOldAndNewEndpoints() {
        String firstEndpoint = endpoint("site-first");
        String secondEndpoint = endpoint("site-second");
        String unrelatedEndpoint = endpoint("site-unrelated");
        LcClient first = LcClientFactory.create(HttpClient.SiteEnum.EN, firstEndpoint);
        LcClient second = LcClientFactory.create(HttpClient.SiteEnum.CN, secondEndpoint);
        LcClient unrelated = LcClientFactory.create(HttpClient.SiteEnum.EN, unrelatedEndpoint);

        CacheInvalidationCoordinator.invalidateSiteChange(
                "leetcode.com",
                firstEndpoint,
                "leetcode.cn",
                secondEndpoint
        );
        CacheInvalidationCoordinator.invalidateSiteChange(
                "leetcode.com", firstEndpoint, "leetcode.cn", secondEndpoint);

        assertNotSame(first, LcClientFactory.create(HttpClient.SiteEnum.EN, firstEndpoint));
        assertNotSame(second, LcClientFactory.create(HttpClient.SiteEnum.CN, secondEndpoint));
        assertSame(unrelated, LcClientFactory.create(HttpClient.SiteEnum.EN, unrelatedEndpoint));
    }

    private static String endpoint(String name) {
        return "https://coordinator-" + name.replace('_', '-') + "-"
                + System.nanoTime() + ".example.com";
    }
}
