package com.shuzijun.leetcode.plugin.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Striped;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.IdeProxySelector;
import com.intellij.util.net.ProxySettings;
import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.http.DefaultExecutoHttp;
import com.shuzijun.lc.http.HttpClient;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

/**
 * @author shuzijun
 */
public class HttpRequestUtils {

    private static final Cache<HttpRequest, HttpResponse> httpResponseCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();
    private static final Striped<Lock> requestLocks = Striped.lazyWeakLock(128);
    private static final AtomicBoolean edtRequestWarningLogged = new AtomicBoolean();
    private static volatile Function<HttpRequest, HttpResponse> testResponseProvider;

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
        if (url.contains(HttpClient.SiteEnum.EN.defaultEndpoint)) {
            return enLcClient.getClient().getHeader();
        } else {
            return cnLcClient.getClient().getHeader();
        }
    }

    @NotNull
    public static HttpResponse executeGet(HttpRequest httpRequest) {
        HttpResponse testResponse = getTestResponse(httpRequest);
        if (testResponse != null) {
            return testResponse;
        }

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
        HttpResponse testResponse = getTestResponse(httpRequest);
        if (testResponse != null) {
            return testResponse;
        }

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
        HttpResponse testResponse = getTestResponse(httpRequest);
        if (testResponse != null) {
            return testResponse;
        }

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
        Map<String, String> headerMap = getHeader(URLUtils.getLeetcodeHost());
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
        enLcClient.getClient().cookieStore().addCookie(URLUtils.getLeetcodeHost(), cookieList);
    }

    public static void resetHttpclient() {
        enLcClient.getClient().cookieStore().clearCookie(URLUtils.getLeetcodeHost());
    }

    @TestOnly
    public static void setTestResponseProvider(@Nullable Function<HttpRequest, HttpResponse> responseProvider) {
        testResponseProvider = responseProvider;
        httpResponseCache.invalidateAll();
    }

    @Nullable
    private static HttpResponse getTestResponse(HttpRequest httpRequest) {
        Function<HttpRequest, HttpResponse> responseProvider = testResponseProvider;
        return responseProvider == null ? null : responseProvider.apply(httpRequest);
    }


    private static class CacheProcessor {
        public static HttpResponse processor(HttpRequest httpRequest, HttpRequestUtils.Callable<HttpResponse> callable) {
            HttpResponse cachedResponse = httpRequest.isCache() ? httpResponseCache.getIfPresent(httpRequest) : null;
            if (cachedResponse != null) {
                return cachedResponse;
            }
            Application application = ApplicationManager.getApplication();
            if (application != null && application.isDispatchThread()) {
                if (edtRequestWarningLogged.compareAndSet(false, true)) {
                    LogUtils.LOG.warn("Blocked a LeetCode network request on the IDEA UI thread");
                }
                HttpResponse rejectedResponse = new HttpResponse();
                rejectedResponse.setStatusCode(-1);
                return rejectedResponse;
            }
            if (httpRequest.isCache()) {
                Lock requestLock = requestLocks.get(httpRequest);
                requestLock.lock();
                try {
                    HttpResponse cached = httpResponseCache.getIfPresent(httpRequest);
                    if (cached != null) {
                        return cached;
                    }
                    HttpResponse httpResponse = callable.call(httpRequest);
                    if (httpResponse.getStatusCode() == 200) {
                        httpResponseCache.put(httpRequest, httpResponse);
                    }
                    return httpResponse;
                } finally {
                    requestLock.unlock();
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

        private static final long CONNECT_TIMEOUT_SECONDS = 8;
        private static final long READ_WRITE_TIMEOUT_SECONDS = 25;
        private static final long CALL_TIMEOUT_SECONDS = 30;

        private final OkHttpClient requestClient;

        private MyExecutorHttp() {
            requestClient = super.getRequestClient().newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(READ_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(READ_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
        }

        @Override
        public OkHttpClient getRequestClient() {
            return requestClient.newBuilder()
                    .proxySelector(new IdeProxySelector(() -> ProxySettings.getInstance().getProxyConfiguration()))
                    .proxyAuthenticator(Authenticator.JAVA_NET_AUTHENTICATOR)
                    .build();
        }
    }
}
