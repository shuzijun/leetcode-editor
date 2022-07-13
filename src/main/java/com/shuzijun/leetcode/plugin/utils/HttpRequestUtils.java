package com.shuzijun.leetcode.plugin.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.io.HttpRequests;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shuzijun
 */
public class HttpRequestUtils {

    private static final Cache<String, HttpResponse> httpResponseCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

    private static final CookieManager cookieManager = new CookieManager(null, (uri, cookie) -> {
        if (uri == null || cookie == null || uri.getHost().equals("hm.baidu.com")) {
            return false;
        }
        return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
    });

    static {
        CookieHandler.setDefault(cookieManager);
    }

    @NotNull
    public static HttpResponse executeGet(HttpRequest httpRequest) {

        return CacheProcessor.processor(httpRequest, request -> {
            HttpResponse httpResponse = new HttpResponse();
            try {
                HttpRequests.request(request.getUrl()).
                        throwStatusCodeException(false).
                        tuner(new HttpRequestTuner(request)).
                        connect(new HttpResponseProcessor(request, httpResponse));

            } catch (IOException e) {
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
                HttpRequests.post(request.getUrl(), request.getContentType())
                        .throwStatusCodeException(false)
                        .tuner(new HttpRequestTuner(request))
                        .connect(new HttpResponseProcessor(request, httpResponse));
            } catch (IOException e) {
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
                HttpRequests.put(request.getUrl(), request.getContentType())
                        .throwStatusCodeException(false)
                        .tuner(new HttpRequestTuner(request))
                        .connect(new HttpResponseProcessor(request, httpResponse));
            } catch (IOException e) {
                LogUtils.LOG.error("HttpRequestUtils request error:", e);
                httpResponse.setStatusCode(-1);
            }
            return httpResponse;
        });
    }

    public static String getToken() {
        if (cookieManager.getCookieStore().getCookies() == null) {
            return null;
        }
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (StringUtils.isNotBlank(cookie.getDomain()) &&
                    cookie.getDomain().toLowerCase().contains(URLUtils.getLeetcodeHost()) && "csrftoken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }
        return null;
    }

    public static boolean isLogin(Project project) {
        HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodePoints()).request();
        if (response.getStatusCode() == 200) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static void setCookie(List<HttpCookie> cookieList) {

        cookieManager.getCookieStore().removeAll();
        for (HttpCookie cookie : cookieList) {
            cookieManager.getCookieStore().add(null, cookie);
        }
    }

    public static void resetHttpclient() {
        cookieManager.getCookieStore().removeAll();
    }


    private static void defaultHeader(HttpRequest httpRequest) {
        Map<String, String> header = httpRequest.getHeader();
        header.putIfAbsent(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36");
        header.putIfAbsent(HttpHeaders.ACCEPT, "*/*");
        //header.putIfAbsent(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        header.putIfAbsent(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
        header.putIfAbsent("origin", URLUtils.getLeetcodeUrl());
        //header.putIfAbsent(":authority", URLUtils.getLeetcodeHost());
        //header.putIfAbsent(":scheme", "https");
    }

    private static class HttpRequestTuner implements HttpRequests.ConnectionTuner {

        private final HttpRequest httpRequest;

        public HttpRequestTuner(HttpRequest httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        public void tune(@NotNull URLConnection urlConnection) throws IOException {

            if (StringUtils.isNotBlank(getToken()) && (urlConnection.getURL().toString().contains(URLUtils.leetcode) || urlConnection.getURL().toString().contains(URLUtils.leetcodecn))) {
                urlConnection.addRequestProperty("x-csrftoken", getToken());
            }
            urlConnection.addRequestProperty("referer", urlConnection.getURL().toString());
            //urlConnection.addRequestProperty(":path", urlConnection.getURL().getPath());

            defaultHeader(httpRequest);
            httpRequest.getHeader().forEach(urlConnection::addRequestProperty);
        }
    }


    private static class HttpResponseProcessor implements HttpRequests.RequestProcessor<HttpResponse> {

        private final HttpRequest httpRequest;
        private final HttpResponse httpResponse;

        public HttpResponseProcessor(HttpRequest httpRequest, HttpResponse httpResponse) {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
        }

        @Override
        public HttpResponse process(@NotNull HttpRequests.Request request) throws IOException {

            if (StringUtils.isNoneBlank(httpRequest.getBody())) {
                request.write(httpRequest.getBody());
            }

            URLConnection urlConnection = request.getConnection();

            if (!(urlConnection instanceof HttpURLConnection)) {
                httpResponse.setStatusCode(-1);
                return httpResponse;
            } else {
                httpResponse.setStatusCode(((HttpURLConnection) urlConnection).getResponseCode());
            }
            httpResponse.setUrl(urlConnection.getURL().toString());
            try {
                httpResponse.setBody(request.readString());
            } catch (IOException ignore) {
            }
            return httpResponse;
        }
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

}
