package com.shuzijun.leetcode.plugin.utils.io;


import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.NetUtils;
import kotlin.text.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUrlConnectionUtil {

    private static final int BLOCK_SIZE = 16 * 1024;
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([^;]+)");

    public static  BufferExposingByteArrayOutputStream readBytes( InputStream inputStream, @NotNull URLConnection connection, @Nullable ProgressIndicator progressIndicator) throws IOException, ProcessCanceledException {
        int contentLength = connection.getContentLength();
        BufferExposingByteArrayOutputStream out = new BufferExposingByteArrayOutputStream(contentLength > 0 ? contentLength : BLOCK_SIZE);
        NetUtils.copyStreamContent(progressIndicator, inputStream, (OutputStream)out, contentLength);
        return out;
    }
    public static  String readString(@NotNull InputStream inputStream, @NotNull URLConnection connection, @Nullable ProgressIndicator progressIndicator) throws IOException, ProcessCanceledException {
        BufferExposingByteArrayOutputStream byteStream = readBytes(inputStream, connection, progressIndicator);
        if(byteStream.size()== 0){
            return "";
        }else {
            return new String(byteStream.getInternalBuffer(), 0, byteStream.size(), getCharset(connection));
        }
    }


    public static  Charset getCharset(@NotNull URLConnection urlConnection) throws IOException {
        String contentType = urlConnection.getContentType();
        if (StringUtils.isBlank(contentType)) {
            Matcher m = CHARSET_PATTERN.matcher(contentType);
            if (m.find()) {
                try {
                    Charset charset = Charset.forName(StringUtil.unquoteString(m.group(1)));
                    return charset;
                } catch (IllegalArgumentException var5) {
                    throw new IOException("unknown charset (" + contentType + ')', var5);
                }
            }
        }
        return Charsets.UTF_8;
    }



}
