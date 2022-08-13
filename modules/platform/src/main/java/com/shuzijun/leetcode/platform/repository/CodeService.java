package com.shuzijun.leetcode.platform.repository;

/**
 * @author shuzijun
 */
public interface CodeService extends Service {

    /**
     * 打开代码
     *
     * @param titleSlug
     */
    void openCode(String titleSlug);

    /**
     * 打开题目描述
     *
     * @param titleSlug
     * @param isOpen
     */
    void openContent(String titleSlug, boolean isOpen);

    /**
     * 提交代码
     *
     * @param titleSlug
     */
    void SubmitCode(String titleSlug);

    /**
     * 运行代码
     *
     * @param titleSlug
     */
    void RunCodeCode(String titleSlug);
}
