package com.shuzijun.leetcode.plugin.listener;

import com.intellij.util.messages.Topic;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;

/**
 * @author shuzijun
 */
public interface ConfigNotifier {

    @Topic.AppLevel
    Topic<ConfigNotifier> TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_CONFIG_TOPIC, ConfigNotifier.class);

    void change(Config oldConfig, Config newConfig);
}
