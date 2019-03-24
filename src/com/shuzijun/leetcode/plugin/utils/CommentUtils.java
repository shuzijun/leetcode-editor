package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import org.jsoup.Jsoup;

/**
 * @author shuzijun
 */
public class CommentUtils {

    public static String createComment(String html, CodeTypeEnum codeTypeEnum) {
       return codeTypeEnum.getComment()+Jsoup.parse(html.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)","\\\\n")).text().replaceAll("\\\\n", "\n" + codeTypeEnum.getComment());

    }
}
