package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.diagnostic.Logger;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author shuzijun
 */
public class ProxyLoadHtmlResourceHandler extends CefResourceHandlerAdapter {
    private static final Logger LOG = Logger.getInstance(ProxyLoadHtmlResourceHandler.class.getName());

    @NotNull
    private final InputStream myInputStream;
    private final Map<String,String> header ;
    private final int status;

    public ProxyLoadHtmlResourceHandler(@NotNull String html, Map<String,String> header, int status) {
        myInputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        this.header = header;
        this.status = status;
    }

    @Override
    public boolean processRequest(@NotNull CefRequest request, @NotNull CefCallback callback) {
        callback.Continue();
        return true;
    }

    @Override
    public void getResponseHeaders(@NotNull CefResponse response, IntRef response_length, StringRef redirectUrl) {
        header.forEach((key,value) -> {
            response.setHeaderByName(key,value,true);
            if(HttpHeaderNames.CONTENT_TYPE.toString().equals(key.toLowerCase())){
                if(value.indexOf(";")>0){
                    response.setMimeType(value.substring(0,value.indexOf(";")));
                }else {
                    response.setMimeType(value);
                }
            }
        });
        response.setStatus(status);
    }

    @Override
    public boolean readResponse(byte@NotNull[] data_out, int bytes_to_read, IntRef bytes_read, CefCallback callback) {
        try {
            int availableSize = myInputStream.available();
            if (availableSize > 0) {
                int bytesToRead = Math.min(bytes_to_read, availableSize);
                bytesToRead = myInputStream.read(data_out, 0, bytesToRead);
                bytes_read.set(bytesToRead);
                return true;
            }
        }
        catch (IOException e) {
            LOG.error(e);
        }
        bytes_read.set(0);
        try {
            myInputStream.close();
        }
        catch (IOException e) {
            LOG.error(e);
        }
        return false;
    }
}