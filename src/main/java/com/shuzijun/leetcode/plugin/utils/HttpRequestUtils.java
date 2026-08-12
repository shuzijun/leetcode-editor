package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.net.IdeProxySelector;
import com.intellij.util.net.ProxySettings;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.http.DefaultExecutoHttp;
import com.shuzijun.lc.http.ExecutorHttp;
import com.shuzijun.lc.http.HttpRequest;
import com.shuzijun.lc.http.HttpResponse;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shuzijun
 */
public class HttpRequestUtils {

    private static final long MAX_LOG_BODY_BYTES = 64 * 1024;

    private static final AtomicBoolean edtRequestWarningLogged = new AtomicBoolean();
    private static volatile Function<String, HttpResponse> testResponseProvider;

    private static final ExecutorHttp EXECUTOR_HTTP = new MyExecutorHttp();

    @NotNull
    public static HttpResponse get(@NotNull String url) {
        HttpResponse testResponse = getTestResponse(url);
        if (testResponse != null) {
            return testResponse;
        }

        Application application = ApplicationManager.getApplication();
        if (application != null && application.isDispatchThread()) {
            if (edtRequestWarningLogged.compareAndSet(false, true)) {
                LogUtils.LOG.warn("Blocked a LeetCode network request on the IDEA UI thread");
            }
            return new HttpResponse(-1);
        }

        try {
            return EXECUTOR_HTTP.executeGet(HttpRequest.builderGet(url).build());
        } catch (LcException e) {
            LogUtils.LOG.error("HttpRequestUtils request error:", e);
            return new HttpResponse(-1);
        }
    }

    @NotNull
    public static ExecutorHttp getExecutorHttp() {
        return EXECUTOR_HTTP;
    }

    @TestOnly
    public static void setTestResponseProvider(@Nullable Function<String, HttpResponse> responseProvider) {
        testResponseProvider = responseProvider;
    }

    @Nullable
    private static HttpResponse getTestResponse(String url) {
        Function<String, HttpResponse> responseProvider = testResponseProvider;
        return responseProvider == null ? null : responseProvider.apply(url);
    }

    private static class MyExecutorHttp extends DefaultExecutoHttp {

        private static final long CONNECT_TIMEOUT_SECONDS = 8;
        private static final long READ_WRITE_TIMEOUT_SECONDS = 25;
        private static final long CALL_TIMEOUT_SECONDS = 30;

        private final OkHttpClient requestClient;

        private MyExecutorHttp() {
            requestClient = super.getRequestClient().newBuilder()
                    .addInterceptor(new NetworkLogInterceptor())
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

    private static final class NetworkLogInterceptor implements Interceptor {

        @Override
        public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();
            if (!isNetworkLogEnabled()) {
                return chain.proceed(request);
            }

            long startedAt = System.nanoTime();
            LogUtils.LOG.info("[NETWORK] --> " + request.method() + " " + sanitize(request.url().toString()));
            try {
                Response response = chain.proceed(request);
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
                LogUtils.LOG.info("[NETWORK] <-- " + response.code() + " " + request.method()
                        + " " + sanitize(request.url().toString()) + " (" + elapsedMillis + " ms)");
                logResponseBody(response);
                return response;
            } catch (IOException exception) {
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
                LogUtils.LOG.warn("[NETWORK] <-- FAILED " + request.method()
                        + " " + sanitize(request.url().toString()) + " (" + elapsedMillis + " ms)", exception);
                throw exception;
            }
        }

        private static void logResponseBody(Response response) {
            ResponseBody body = response.body();
            if (body == null || !isText(body.contentType())) {
                return;
            }
            try {
                String content = response.peekBody(MAX_LOG_BODY_BYTES).string();
                LogUtils.LOG.info("[NETWORK] body: " + sanitize(content));
            } catch (IOException exception) {
                LogUtils.LOG.debug("[NETWORK] Failed to read response body", exception);
            }
        }

        private static boolean isText(MediaType contentType) {
            if (contentType == null) {
                return true;
            }
            String type = contentType.type().toLowerCase(Locale.ROOT);
            String subtype = contentType.subtype().toLowerCase(Locale.ROOT);
            return "text".equals(type)
                    || subtype.contains("json")
                    || subtype.contains("xml")
                    || subtype.contains("javascript")
                    || subtype.contains("html")
                    || subtype.contains("form");
        }
    }

    static boolean isNetworkLogEnabled() {
        return DevelopmentTools.isEnabled();
    }

    private static final Pattern SENSITIVE_JSON_VALUE = Pattern.compile(
            "(?i)(\"(?:authorization|cookie|set-cookie|password|passwd|csrf(?:token)?|token|access_token|refresh_token|session|leetcode_session)\"\\s*:\\s*)\"(?:\\\\.|[^\"\\\\])*\""
    );
    private static final Pattern SENSITIVE_PARAMETER = Pattern.compile(
            "(?i)((?:authorization|cookie|password|passwd|csrf(?:token)?|token|access_token|refresh_token|session|leetcode_session)=)(?:bearer\\s+)?[^&\\s]*"
    );
    private static final Pattern BEARER_TOKEN = Pattern.compile(
            "(?i)(bearer\\s+)[a-z0-9._~+/=-]+"
    );

    static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String sanitized = SENSITIVE_JSON_VALUE.matcher(value)
                .replaceAll("$1\"<redacted>\"");
        sanitized = replaceSensitiveParameters(sanitized);
        return BEARER_TOKEN.matcher(sanitized).replaceAll("$1<redacted>");
    }

    private static String replaceSensitiveParameters(String value) {
        Matcher matcher = SENSITIVE_PARAMETER.matcher(value);
        StringBuffer sanitized = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(
                    sanitized,
                    Matcher.quoteReplacement(matcher.group(1) + "<redacted>")
            );
        }
        matcher.appendTail(sanitized);
        return sanitized.toString();
    }
}
