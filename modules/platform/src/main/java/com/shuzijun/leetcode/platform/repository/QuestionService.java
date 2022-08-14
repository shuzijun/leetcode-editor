package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.platform.model.*;

import java.util.List;

/**
 * 题目服务
 *
 * @author shuzijun
 */
public interface QuestionService extends Service {

    /**
     * 获取题目列表
     *
     * @param pageInfo
     * @return
     */
    PageInfo<QuestionView> getQuestionViewList(PageInfo<QuestionView> pageInfo);

    /**
     * 获取所有题目
     *
     * @param reset
     * @return
     */
    List<QuestionView> getQuestionAllService(boolean reset);

    /**
     * 获取在所有题目的位置
     *
     * @param titleSlug
     * @return
     */
    QuestionIndex getQuestionIndex(String titleSlug);

    /**
     * 获取每日题目
     *
     * @return
     */
    QuestionView questionOfToday();

    /**
     * 随机一个题目
     *
     * @param pageInfo
     * @return
     */
    Question pick(PageInfo<?> pageInfo);

    /**
     * 获取用户数据
     *
     * @return
     */
    User getUser();

    /**
     * 根据标识获取题目
     *
     * @param titleSlug
     * @return
     */
    Question getQuestionByTitleSlug(String titleSlug);
}
