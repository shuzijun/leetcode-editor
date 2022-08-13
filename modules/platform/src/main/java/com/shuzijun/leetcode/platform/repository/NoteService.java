package com.shuzijun.leetcode.platform.repository;

import java.io.File;

/**
 * 笔记服务
 *
 * @author shuzijun
 */
public interface NoteService extends Service {

    /**
     * 显示笔记
     *
     * @param titleSlug
     * @param isOpenEditor
     * @return
     */
    File show(String titleSlug, Boolean isOpenEditor);

    /**
     * 拉取笔记
     *
     * @param titleSlug
     * @return
     */
    boolean pull(String titleSlug);

    /**
     * 推送笔记
     *
     * @param titleSlug
     */
    void push(String titleSlug);

}
