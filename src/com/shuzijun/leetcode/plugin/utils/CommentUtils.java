package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import org.jsoup.Jsoup;

/**
 * @author shuzijun
 */
public class CommentUtils {

    public static String createComment(String html, CodeTypeEnum codeTypeEnum) {

       return codeTypeEnum.getComment()+Jsoup.parse(html).text().replaceAll("\n", "\n" + codeTypeEnum.getComment());

    }

    public static void main(String[] args) {
        String a= "<p>Given an array of integers, return <strong>indices</strong> of the two numbers such that they add up to a specific target.</p>\\r\\n\\r\\n<p>You may assume that each input would have <strong><em>exactly</em></strong> one solution, and you may not use the <em>same</em> element twice.</p>\\r\\n\\r\\n<p><strong>Example:</strong></p>\\r\\n\\r\\n<pre>\\r\\nGiven nums = [2, 7, 11, 15], target = 9,\\r\\n\\r\\nBecause nums[<strong>0</strong>] + nums[<strong>1</strong>] = 2 + 7 = 9,\\r\\nreturn [<strong>0</strong>, <strong>1</strong>].\\r\\n</pre>\\r\\n\\r\\n<p>&nbsp;</p>\\r\\n";
        System.out.println(createComment(a,CodeTypeEnum.JAVA));
    }
}
