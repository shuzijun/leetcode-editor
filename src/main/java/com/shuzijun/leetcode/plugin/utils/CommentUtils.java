package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shuzijun
 */
public class CommentUtils {

    private static final Pattern subPattern = Pattern.compile("<sup>(<span.*>?)?([0-9abcdeghijklmnoprstuvwxyz\\+\\-\\*=\\(\\)\\.\\/]+)(</span>)?</sup>?");

    public static String createComment(String html, CodeTypeEnum codeTypeEnum, Config config) {
        html = html.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "\\\\n").replaceAll(" "," ");
        if(config.getHtmlContent()) {
            if(config.getMultilineComment()){
                return String.format(codeTypeEnum.getMultiLineComment(),html.replaceAll("\\\\n", "\n"));
            }else {
               return codeTypeEnum.getComment() + html.replaceAll("\\\\n", "\n" + codeTypeEnum.getComment());
            }
        }
        Matcher subMatcher = subPattern.matcher(html);
        while (subMatcher.find()) {
            String subStr = SuperscriptUtils.getSup(subMatcher.group(2));
            html = html.replace(subMatcher.group(), "<sup>" + subStr + "</sup>");
        }
        String comment = config.getMultilineComment()?"":codeTypeEnum.getComment();
        String body = comment + Jsoup.parse(html).text().replaceAll("\\\\n", "\n" + comment);
        String[] lines = body.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int c = line.length() / 80;
            if (c == 0) {
                sb.append(line).append("\n");
            } else {
                StringBuilder lineBuilder = new StringBuilder(line);
                for (int i = c; i > 0; i--) {
                    int idx = 80 * i;
                    while (idx > (80 * i - 20)) {
                        if (isSplit(lineBuilder.charAt(idx - 1))) {
                            break;
                        }
                        idx = idx - 1;
                    }
                    lineBuilder.insert(idx, "\n" + comment);
                }
                sb.append(lineBuilder).append("\n");
            }
        }
        return config.getMultilineComment()?String.format(codeTypeEnum.getMultiLineComment(),sb):sb.toString();
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

    private static boolean isSplit(char c) {
        if (c == 34 || c == 39 || (c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
            return false;
        }
        return true;
    }
}
