package com.shuzijun.leetcode.plugin.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shuzijun
 */
public class HttpClientUtils {

    private final static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);


    private static CloseableHttpClient httpclient = null;
    private static HttpClientContext context = null;
    private static RequestConfig requestConfig = null;

    private static void creatHttpClient() {
        if (httpclient == null) {

            RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
            context = HttpClientContext.create();
            BasicCookieStore cookieStore = new BasicCookieStore();
            context.setCookieStore(cookieStore);

            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000)
                    .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();

            httpclient = HttpClients.custom()
                    .setDefaultRequestConfig(globalConfig)
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultHeaders(defaultHeader())
                    .setConnectionManager(getconnectionManager())
                    .build();

        }
    }

    public static CloseableHttpResponse executeGet(HttpGet httpGet) {
        if (httpclient == null) {
            creatHttpClient();
        }
        httpGet.setConfig(requestConfig);
        return execute(httpGet);

    }

    public static CloseableHttpResponse executePost(HttpPost httpPost) {
        if (httpclient == null) {
            creatHttpClient();
        }
        httpPost.setConfig(requestConfig);
        return execute(httpPost);
    }

    public static CloseableHttpResponse execute(HttpUriRequest httpUriRequest) {

        if (StringUtils.isNotBlank(getToken())) {
            httpUriRequest.setHeader("x-csrftoken", getToken());
        }

        httpUriRequest.setHeader("referer", httpUriRequest.getURI().toString());
        httpUriRequest.setHeader(":path", httpUriRequest.getURI().getPath());

        try {
            CloseableHttpResponse response = httpclient.execute(httpUriRequest, context);
            return response;
        } catch (IOException e) {
            logger.error("请求出错:", e);
        } finally {
        }

        return null;

    }

    private static List<Header> defaultHeader() {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
        //headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9"));
        headers.add(new BasicHeader("origin", URLUtils.getLeetcodeUrl()));
        headers.add(new BasicHeader(":authority", URLUtils.getLeetcodeHost()));
        headers.add(new BasicHeader(":scheme", "https"));

        return headers;
    }

    public static boolean isLogin() {
        HttpGet httpget = new HttpGet(URLUtils.getLeetcodePoints());
        CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
        httpget.abort();
        if (response == null) {
            return Boolean.FALSE;
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static String getToken() {
        if (context == null || context.getCookieStore().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : context.getCookieStore().getCookies()) {
            if ("csrftoken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }
        return null;
    }

    public static void resetHttpclient() {
        if (httpclient != null) {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpclient = null;
            }
        }
    }

    private static PoolingHttpClientConnectionManager getconnectionManager() {

        try {
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String str) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String str) {
                }
            };

            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{trustManager}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory).build();
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            return connectionManager;

        } catch (Exception e) {
            logger.error("创建PoolingHttpClientConnectionManager失败", e);
        }
        return new PoolingHttpClientConnectionManager();
    }


}
