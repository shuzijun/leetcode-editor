package com.shuzijun.leetcode.plugin.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideAuthenticator;
import com.intellij.util.net.IdeaWideProxySelector;
import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.http.DefaultExecutoHttp;
import com.shuzijun.lc.http.HttpClient;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author shuzijun
 */
public class HttpRequestUtils {

    private static final Cache<String, HttpResponse> httpResponseCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

    private static MyExecutorHttp executorHttp = new MyExecutorHttp();
    private static LcClient enLcClient = LcClient.builder(HttpClient.SiteEnum.EN).executorHttp(executorHttp).build();
    private static LcClient cnLcClient = LcClient.builder(HttpClient.SiteEnum.CN).executorHttp(executorHttp).build();
    private static final CookieManager cookieManager = new CookieManager(null, (uri, cookie) -> {
        if (uri == null || cookie == null || uri.getHost().equals("hm.baidu.com")) {
            return false;
        }
        return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
    });

    static {
        CookieHandler.setDefault(cookieManager);
    }

    private static HttpResponse buildResp(com.shuzijun.lc.http.HttpResponse response, HttpResponse httpResponse) {
        httpResponse.setUrl(response.getHttpRequest().getUrl());
        httpResponse.setStatusCode(response.getStatusCode());
        httpResponse.setBody(response.getBody());
        return httpResponse;
    }

    private static Map<String, String> getHeader(String url) {
        if (url.contains(HttpClient.SiteEnum.EN.defaultEndpoint)){
            return enLcClient.getClient().getHeader();
        } else {
            return cnLcClient.getClient().getHeader();
        }
    }

    @NotNull
    public static HttpResponse executeGet(HttpRequest httpRequest) {

        return CacheProcessor.processor(httpRequest, request -> {

            HttpResponse httpResponse = new HttpResponse();
            try {
                com.shuzijun.lc.http.HttpRequest.HttpRequestBuilder builder = com.shuzijun.lc.http.HttpRequest.
                        builderGet(request.getUrl()).body(request.getBody()).addHeader(getHeader(request.getUrl()));
                if (request.getHeader() != null) {
                    builder.addHeader(request.getHeader());
                }
                return buildResp(executorHttp.executeGet(builder.build()), httpResponse);

            } catch (LcException e) {
                LogUtils.LOG.error("HttpRequestUtils request error:", e);
                httpResponse.setStatusCode(-1);
            }
            return httpResponse;
        });


    }

    @NotNull
    public static HttpResponse executePost(HttpRequest httpRequest) {
        return CacheProcessor.processor(httpRequest, request -> {
            HttpResponse httpResponse = new HttpResponse();
            try {
                com.shuzijun.lc.http.HttpRequest.HttpRequestBuilder builder = com.shuzijun.lc.http.HttpRequest.
                        builderPost(request.getUrl(), request.getContentType()).body(request.getBody()).addHeader(getHeader(request.getUrl()));
                if (request.getHeader() != null) {
                    builder.addHeader(request.getHeader());
                }
                return buildResp(executorHttp.executePost(builder.build()), httpResponse);
            } catch (LcException e) {
                LogUtils.LOG.error("HttpRequestUtils request error:", e);
                httpResponse.setStatusCode(-1);
            }
            return httpResponse;
        });
    }

    public static HttpResponse executePut(HttpRequest httpRequest) {
        return CacheProcessor.processor(httpRequest, request -> {
            HttpResponse httpResponse = new HttpResponse();
            try {
                com.shuzijun.lc.http.HttpRequest.HttpRequestBuilder builder = com.shuzijun.lc.http.HttpRequest.
                        builderPut(request.getUrl(), request.getContentType()).body(request.getBody()).addHeader(getHeader(request.getUrl()));
                if (request.getHeader() != null) {
                    builder.addHeader(request.getHeader());
                }
                return buildResp(executorHttp.executePut(builder.build()), httpResponse);
            } catch (LcException e) {
                LogUtils.LOG.error("HttpRequestUtils request error:", e);
                httpResponse.setStatusCode(-1);
            }
            return httpResponse;
        });
    }

    public static String getToken() {
        Map<String,String> headerMap = getHeader(URLUtils.getLeetcodeHost());
        return headerMap.get("x-csrftoken");
    }

    public static boolean isLogin(Project project) {
        HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodePoints()).request();
        if (response.getStatusCode() == 200) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static void setCookie(List<HttpCookie> cookieList) {
        enLcClient.getClient().cookieStore().clearCookie(URLUtils.getLeetcodeHost());
        enLcClient.getClient().cookieStore().addCookie(URLUtils.getLeetcodeHost(),cookieList);
    }

    public static void resetHttpclient() {
        enLcClient.getClient().cookieStore().clearCookie(URLUtils.getLeetcodeHost());
    }


    private static class CacheProcessor {
        public static HttpResponse processor(HttpRequest httpRequest, HttpRequestUtils.Callable<HttpResponse> callable) {

            String key = httpRequest.hashCode() + "";
            if (httpRequest.isCache() && httpResponseCache.getIfPresent(key) != null) {
                return httpResponseCache.getIfPresent(key);
            }
            if (httpRequest.isCache()) {
                synchronized (key.intern()) {
                    if (httpResponseCache.getIfPresent(key) != null) {
                        return httpResponseCache.getIfPresent(key);
                    } else {
                        HttpResponse httpResponse = callable.call(httpRequest);
                        if (httpResponse.getStatusCode() == 200) {
                            httpResponseCache.put(key, httpResponse);
                        }
                        return httpResponse;
                    }
                }
            } else {
                return callable.call(httpRequest);

            }
        }
    }

    @FunctionalInterface
    private interface Callable<V> {
        V call(HttpRequest request);
    }


    private static class MyExecutorHttp extends DefaultExecutoHttp {
        @Override
        public OkHttpClient getRequestClient() {
            final HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
            if (!httpConfigurable.USE_HTTP_PROXY &&  !httpConfigurable.USE_PROXY_PAC) {
                return super.getRequestClient();
            }
            final IdeaWideProxySelector ideaWideProxySelector = new IdeaWideProxySelector(httpConfigurable);
            OkHttpClient.Builder builder = super.getRequestClient().newBuilder().proxySelector(ideaWideProxySelector);
            if (httpConfigurable.PROXY_AUTHENTICATION) {
                final IdeaWideAuthenticator ideaWideAuthenticator = new IdeaWideAuthenticator(httpConfigurable);
                final okhttp3.Authenticator proxyAuthenticator = getProxyAuthenticator(ideaWideAuthenticator);
                builder.proxyAuthenticator(proxyAuthenticator);
            }
            return builder.build();
        }

        private okhttp3.Authenticator getProxyAuthenticator(IdeaWideAuthenticator ideaWideAuthenticator) {
            okhttp3.Authenticator proxyAuthenticator = null;

            if (Objects.nonNull(ideaWideAuthenticator)) {
                proxyAuthenticator = (route, response) -> {
                    final PasswordAuthentication authentication = ideaWideAuthenticator.getPasswordAuthentication();
                    final String credential = Credentials.basic(authentication.getUserName(), Arrays.toString(authentication.getPassword()));
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                };
            }
            return proxyAuthenticator;
        }
    }
}

