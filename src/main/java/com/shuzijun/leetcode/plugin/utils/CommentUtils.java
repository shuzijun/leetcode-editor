package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

/**
 * @author shuzijun
 */
public class CommentUtils {

    public static String createComment(String html, CodeTypeEnum codeTypeEnum) {
        String body = codeTypeEnum.getComment() + Jsoup.parse(html.replaceAll("(\\n\\n|\\r\\n|\\r|\\n|\\n\\r)", "\\\\n")).text().replaceAll("\\\\n", "\n" + codeTypeEnum.getComment());
        String[] lines = body.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int c = line.length() / 80;
            if (c == 0) {
                sb.append(line).append("\n");
            } else {
                StringBuilder lineBuilder = new StringBuilder(line);
                for (int i = c; i > 0; i--) {
                    lineBuilder.insert(80 * i, "\n" + codeTypeEnum.getComment());
                }
                sb.append(lineBuilder).append("\n");
            }
        }
        return sb.toString();
    }

    public static String createSubmissions(String html) {
        String pageData = StringUtils.substringBetween(html, "var pageData =", "if (isNaN(pageData.submissionData.status_code))");
        if (StringUtils.isBlank(pageData)) {
            return pageData;
        }
        pageData = pageData.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "");
        pageData = pageData.substring(0, pageData.length() - 1);
        pageData = pageData.replaceAll("status_code: parseInt\\('\\d+', \\d+\\),", "");
        return pageData;
    }
}
