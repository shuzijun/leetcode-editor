package com.shuzijun.leetcode.platform.notifier;

import com.shuzijun.leetcode.platform.model.Config;

/**
 * @author shuzijun
 */
public interface ConfigNotifier {
    void change(Config oldConfig, Config newConfig);
}
