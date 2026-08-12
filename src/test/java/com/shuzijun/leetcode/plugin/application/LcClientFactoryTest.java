package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.http.HttpClient;
import org.junit.Test;

import java.net.HttpCookie;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class LcClientFactoryTest {

    @Test
    public void reusesClientAndCookieStateForSameSiteAndEndpoint() throws Exception {
        String endpoint = "https://factory-test-" + System.nanoTime() + ".example.com";
        LcClient first = LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint);
        HttpCookie csrf = new HttpCookie("csrftoken", "shared-token");

        first.api().account().setCookies(Collections.singletonList(csrf), RequestContext.DEFAULT);
        LcClient second = LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint);

        assertSame(first, second);
        assertEquals("shared-token", second.api().account().csrfToken(RequestContext.DEFAULT));
    }

    @Test
    public void invalidatesOnlyClientsForRequestedEndpoint() {
        String suffix = String.valueOf(System.nanoTime());
        String firstEndpoint = "https://factory-first-" + suffix + ".example.com";
        String secondEndpoint = "https://factory-second-" + suffix + ".example.com";
        LcClient firstEn = LcClientFactory.create(HttpClient.SiteEnum.EN, firstEndpoint);
        LcClient firstCn = LcClientFactory.create(HttpClient.SiteEnum.CN, firstEndpoint);
        LcClient secondEn = LcClientFactory.create(HttpClient.SiteEnum.EN, secondEndpoint);

        LcClientFactory.invalidateEndpoint(firstEndpoint);

        assertNotSame(firstEn, LcClientFactory.create(HttpClient.SiteEnum.EN, firstEndpoint));
        assertNotSame(firstCn, LcClientFactory.create(HttpClient.SiteEnum.CN, firstEndpoint));
        assertSame(secondEn, LcClientFactory.create(HttpClient.SiteEnum.EN, secondEndpoint));
    }

    @Test
    public void invalidatesOnlyClientsForRequestedSite() {
        String endpoint = "https://factory-site-" + System.nanoTime() + ".example.com";
        LcClient en = LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint);
        LcClient cn = LcClientFactory.create(HttpClient.SiteEnum.CN, endpoint);

        LcClientFactory.invalidateSite(HttpClient.SiteEnum.EN);

        assertNotSame(en, LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint));
        assertSame(cn, LcClientFactory.create(HttpClient.SiteEnum.CN, endpoint));
    }

    @Test
    public void invalidatesAllClientsIdempotently() {
        String endpoint = "https://factory-all-" + System.nanoTime() + ".example.com";
        LcClient client = LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint);

        LcClientFactory.invalidateAll();
        LcClientFactory.invalidateAll();

        assertNotSame(client, LcClientFactory.create(HttpClient.SiteEnum.EN, endpoint));
    }
}
