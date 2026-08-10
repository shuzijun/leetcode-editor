package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtilRt;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.FileResponses;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class ResourcesController extends BaseController {

    private static final Logger LOG = Logger.getInstance(ResourcesController.class);
    private static final int MAX_CACHE_ENTRIES = 32;
    private static final Map<String, CachedResource> CACHE = new LinkedHashMap<>(MAX_CACHE_ENTRIES, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedResource> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    private final String controllerPath = "resources";

    @Override
    public String getControllerPath() {
        return controllerPath;
    }

    @Override
    public FullHttpResponse get(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) {
        String resourceName = getResourceName(urlDecoder);
        CachedResource resource;
        synchronized (CACHE) {
            resource = CACHE.get(resourceName);
        }
        if (resource == null) {
            resource = load(resourceName);
            if (resource == null) {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER);
            }
            synchronized (CACHE) {
                CachedResource existing = CACHE.putIfAbsent(resourceName, resource);
                if (existing != null) {
                    resource = existing;
                }
            }
        }

        String ifNoneMatch = request.headers().get(HttpHeaderNames.IF_NONE_MATCH);
        if (resource.etag.equals(ifNoneMatch)) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_MODIFIED,
                    Unpooled.EMPTY_BUFFER
            );
            applyCacheHeaders(response, resource);
            return response;
        }

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(resource.data).asReadOnly()
        );
        applyCacheHeaders(response, resource);
        return response;
    }

    private CachedResource load(String resourceName) {
        try (InputStream inputStream = PreviewStaticServer.class.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return null;
            }
            byte[] data = FileUtilRt.loadBytes(inputStream);
            return new CachedResource(
                    data,
                    FileResponses.INSTANCE.getContentType(resourceName) + "; charset=utf-8",
                    '"' + sha256(data) + '"'
            );
        } catch (IOException e) {
            LOG.warn(e);
            return null;
        }
    }

    private static void applyCacheHeaders(FullHttpResponse response, CachedResource resource) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, resource.contentType);
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=3600, public");
        response.headers().set(HttpHeaderNames.ETAG, resource.etag);
    }

    private static String sha256(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder value = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                value.append(String.format("%02x", item));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    static void clearCacheForTests() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    private static final class CachedResource {
        private final byte[] data;
        private final String contentType;
        private final String etag;

        private CachedResource(byte[] data, String contentType, String etag) {
            this.data = data;
            this.contentType = contentType;
            this.etag = etag;
        }
    }
}
