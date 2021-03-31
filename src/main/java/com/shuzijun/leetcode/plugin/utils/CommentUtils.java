package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shuzijun
 */
public class CommentUtils {

    private static final Pattern subPattern = Pattern.compile("<sup>(<span.*>?)?([0-9abcdeghijklmnoprstuvwxyz\\+\\-\\*=\\(\\)\\.\\/]+)(</span>)?</sup>?");

    public static String createComment(String html, CodeTypeEnum codeTypeEnum) {
        Matcher subMatcher = subPattern.matcher(html);
        while (subMatcher.find()) {
            String subStr = SuperscriptUtils.getSup(subMatcher.group(2));
            html = html.replace(subMatcher.group(), "<sup>" + subStr + "</sup>");
        }
        html = html.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "\\\\n").replaceAll(" "," ");
        String body = codeTypeEnum.getComment() + Jsoup.parse(html).text().replaceAll("\\\\n", "\n" + codeTypeEnum.getComment());
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
