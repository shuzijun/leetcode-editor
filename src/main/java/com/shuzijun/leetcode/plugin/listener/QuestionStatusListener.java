package com.shuzijun.leetcode.plugin.listener;

import com.intellij.util.messages.Topic;
import com.shuzijun.leetcode.plugin.model.Question;

/**
 * @author shuzijun
 */
public interface QuestionStatusListener {

    @Topic.ProjectLevel
    Topic<QuestionStatusListener> QUESTION_STATUS_TOPIC = Topic.create("Question Status", QuestionStatusListener.class);

    public void updateTable(Question question);


}
