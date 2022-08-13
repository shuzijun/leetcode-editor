package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.plugin.model.Tag;

/**
 * 收藏服务
 *
 * @author shuzijun
 */
public interface FavoriteService extends Service {
    /**
     * 添加收藏
     *
     * @param tag
     * @param titleSlug
     */
    void addQuestionToFavorite(Tag tag, String titleSlug);

    /**
     * 移除收藏
     *
     * @param tag
     * @param titleSlug
     */
    void removeQuestionFromFavorite(Tag tag, String titleSlug);
}
