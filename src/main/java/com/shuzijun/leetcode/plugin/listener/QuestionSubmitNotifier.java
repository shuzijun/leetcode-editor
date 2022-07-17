package com.shuzijun.leetcode.plugin.listener;

import com.intellij.util.messages.Topic;
import com.shuzijun.leetcode.plugin.model.PluginConstant;

/**
 * @author shuzijun
 */
public interface QuestionSubmitNotifier {

    @Topic.AppLevel
    Topic<QuestionSubmitNotifier> TOPIC = Topic.create(PluginConstant.LEETCODE_EDITOR_QUESTION_SUBMIT_TOPIC, QuestionSubmitNotifier.class);

    void submit(String host, String slug);
}
