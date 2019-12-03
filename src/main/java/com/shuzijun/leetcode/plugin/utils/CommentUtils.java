package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

/**
 * @author shuzijun
 */
public class CommentUtils {

    public static String createComment(String html, CodeTypeEnum codeTypeEnum) {
        return codeTypeEnum.getComment() + Jsoup.parse(html.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "\\\\n")).text().replaceAll("\\\\n", "\n" + codeTypeEnum.getComment());

    }

    public static String createSubmissions(String html) {
        String pageData = StringUtils.substringBetween(html, "var pageData =", "if (isNaN(pageData.submissionData.status_code))");
        if (StringUtils.isBlank(pageData)) {
            return pageData;
        }
        pageData = pageData.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "");
        pageData = pageData.substring(0, pageData.length() - 1);
        pageData = pageData.replaceAll("status_code: parseInt\\('\\d+', \\d+\\),","");
        return pageData;
    }
}
