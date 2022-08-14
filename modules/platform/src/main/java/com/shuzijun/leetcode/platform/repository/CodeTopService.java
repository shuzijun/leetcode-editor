package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.extension.NavigatorAction;

/**
 * @author shuzijun
 */
public interface CodeTopService extends Service {

    /**
     * 加载数据
     *
     * @param navigatorAction
     */
    void loadServiceData(NavigatorAction navigatorAction);

    /**
     * 加载数据并选中
     *
     * @param navigatorAction
     * @param selectTitleSlug
     */
    void loadServiceData(NavigatorAction navigatorAction, String selectTitleSlug);
}
