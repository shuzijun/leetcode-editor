package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.platform.extension.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.PageInfo;

/**
 * 视图服务
 *
 * @author shuzijun
 */
public interface ViewService extends Service {

    /**
     * 加载分页数据
     *
     * @param navigatorAction
     */
    void loadServiceData(NavigatorAction navigatorAction);

    /**
     * 加载分页数据
     *
     * @param navigatorAction
     * @param selectTitleSlug 需要选择的标记
     */
    void loadServiceData(NavigatorAction navigatorAction, String selectTitleSlug);

    /**
     * 随机打开一个数据
     *
     * @param pageInfo
     */
    void pick(PageInfo pageInfo);

    /**
     * 加载所有数据
     *
     * @param navigatorAction
     */
    void loadAllServiceData(NavigatorAction navigatorAction);

    /**
     * 加载所有数据
     *
     * @param navigatorAction
     * @param selectTitleSlug 需要选择的标记
     * @param reset
     */
    void loadAllServiceData(NavigatorAction navigatorAction, String selectTitleSlug, boolean reset);
}
