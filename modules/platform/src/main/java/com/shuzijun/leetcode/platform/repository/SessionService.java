package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.platform.model.Session;

import java.util.List;

/**
 * 进度服务
 *
 * @author shuzijun
 */
public interface SessionService extends Service {

    /**
     * 获取进度
     *
     * @return
     */
    List<Session> getSession();

    /**
     * 获取进度
     *
     * @param cache
     * @return
     */
    List<Session> getSession(boolean cache);

    /**
     * 切换进度
     *
     * @param id
     * @return
     */
    boolean switchSession(Integer id);
}
