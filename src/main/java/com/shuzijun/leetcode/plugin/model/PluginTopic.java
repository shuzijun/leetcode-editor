package com.shuzijun.leetcode.plugin.model;

import com.intellij.util.messages.Topic;
import com.shuzijun.leetcode.platform.notifier.*;

public class PluginTopic {
    @Topic.AppLevel
    public static Topic<AllQuestionNotifier> ALL_QUESTION_TOPIC = Topic.create(PluginConstant.LEETCODE_ALL_QUESTION_TOPIC, AllQuestionNotifier.class);

    @Topic.AppLevel
    public static Topic<ConfigNotifier> CONFIG_TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_CONFIG_TOPIC, ConfigNotifier.class);

    @Topic.AppLevel
    public static Topic<LoginNotifier> LOGIN_TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_LOGIN_TOPIC, LoginNotifier.class);

    @Topic.AppLevel
    public static Topic<QuestionStatusNotifier> QUESTION_STATUS_TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_QUESTION_STATUS_TOPIC, QuestionStatusNotifier.class);

    @Topic.AppLevel
    public static Topic<QuestionSubmitNotifier> QUESTION_SUBMIT_TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_QUESTION_SUBMIT_TOPIC, QuestionSubmitNotifier.class);
}
