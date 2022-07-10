package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import com.shuzijun.leetcode.plugin.model.PluginConstant;

/**
 * @author shuzijun
 */
public interface LoginNotifier {

    @Topic.AppLevel
    Topic<LoginNotifier> TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_LOGIN_TOPIC, LoginNotifier.class);

    void login(Project project, String host);

    void logout(Project project, String host);
}
