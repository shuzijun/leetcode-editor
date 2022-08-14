package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.platform.model.Solution;

import java.io.File;
import java.util.List;

/**
 * 文章服务
 *
 * @author shuzijun
 */
public interface ArticleService extends Service {

    /**
     * 打开解题文章
     *
     * @param titleSlug
     * @param articleSlug
     * @param isOpenEditor
     * @return
     */
    File openArticle(String titleSlug, String articleSlug, Boolean isOpenEditor);

    /**
     * 获取解题文章
     *
     * @param titleSlug
     * @param articleSlug
     * @return
     */
    String getArticle(String titleSlug, String articleSlug);

    /**
     * 格式化文章
     *
     * @param content
     * @param host
     * @return
     */
    String formatMarkdown(String content, String host);

    /**
     * 获取解题文章列表
     *
     * @param titleSlug
     * @return
     */
    List<Solution> getSolutionList(String titleSlug);
}
