package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.platform.model.Config;

/**
 * 获取配置数据
 */
public interface ConfigService extends Service {

    Config getConfig();
}
