package com.shuzijun.leetcode.platform.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.io.HttpRequests;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import com.shuzijun.leetcode.plugin.model.HttpResponse;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
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
public class HttpRequestService {

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

    private URLService urlService;

    private HttpRequestService(URLService urlService) {
        this.urlService = urlService;
    }

    public static HttpRequestService getInstance(URLService urlService) {
        return new HttpRequestService(urlService);
    }

    public static void resetHttpclient() {
        cookieManager.getCookieStore().removeAll();
    }

    @NotNull
    public HttpResponse executeGet(HttpRequest httpRequest) {

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
    public HttpResponse executePost(HttpRequest httpRequest) {
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

    public HttpResponse executePut(HttpRequest httpRequest) {
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

    public String getToken() {
        if (cookieManager.getCookieStore().getCookies() == null) {
            return null;
        }
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (StringUtils.isNotBlank(cookie.getDomain()) &&
                    cookie.getDomain().toLowerCase().contains(urlService.getLeetcodeHost()) && "csrftoken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }
        return null;
    }

    public boolean isLogin(Project project) {
        HttpResponse response = HttpRequest.builder(this).get(urlService.getLeetcodePoints()).request();
        if (response.getStatusCode() == 200) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void setCookie(List<HttpCookie> cookieList) {

        cookieManager.getCookieStore().removeAll();
        for (HttpCookie cookie : cookieList) {
            cookieManager.getCookieStore().add(null, cookie);
        }
    }

    private void defaultHeader(HttpRequest httpRequest) {
        Map<String, String> header = httpRequest.getHeader();
        header.putIfAbsent(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36");
        header.putIfAbsent(HttpHeaders.ACCEPT, "*/*");
        //header.putIfAbsent(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        header.putIfAbsent(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
        header.putIfAbsent("origin", urlService.getLeetcodeUrl());
        //header.putIfAbsent(":authority", URLUtils.getLeetcodeHost());
        //header.putIfAbsent(":scheme", "https");
    }

    @FunctionalInterface
    private interface Callable<V> {
        V call(HttpRequest request);
    }

    private static class CacheProcessor {
        public static HttpResponse processor(HttpRequest httpRequest, Callable<HttpResponse> callable) {

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

    private class HttpRequestTuner implements HttpRequests.ConnectionTuner {

        private final HttpRequest httpRequest;

        public HttpRequestTuner(HttpRequest httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        public void tune(@NotNull URLConnection urlConnection) throws IOException {

            if (StringUtils.isNotBlank(getToken()) && (urlConnection.getURL().toString().contains(urlService.leetcode) || urlConnection.getURL().toString().contains(urlService.leetcodecn))) {
                urlConnection.addRequestProperty("x-csrftoken", getToken());
            }
            urlConnection.addRequestProperty("referer", urlConnection.getURL().toString());
            //urlConnection.addRequestProperty(":path", urlConnection.getURL().getPath());

            defaultHeader(httpRequest);
            httpRequest.getHeader().forEach(urlConnection::addRequestProperty);
        }
    }

    private class HttpResponseProcessor implements HttpRequests.RequestProcessor<HttpResponse> {

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

}
