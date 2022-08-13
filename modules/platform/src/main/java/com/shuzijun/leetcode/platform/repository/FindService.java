package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.plugin.model.Tag;

import java.util.List;

/**
 * 查找相关服务
 *
 * @author shuzijun
 */
public interface FindService extends Service {

    /**
     * 难度
     *
     * @return
     */
    List<Tag> getDifficulty();

    /**
     * 状态
     *
     * @return
     */
    List<Tag> getStatus();

    /**
     * 标签
     *
     * @return
     */
    List<Tag> getTags();

    /**
     * 列表
     *
     * @return
     */
    List<Tag> getLists();

    /**
     * 类型
     *
     * @return
     */
    List<Tag> getCategory();

}
