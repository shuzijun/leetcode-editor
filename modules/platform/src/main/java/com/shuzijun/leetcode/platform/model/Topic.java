package com.shuzijun.leetcode.platform.model;

import com.shuzijun.leetcode.platform.notifier.*;

public class Topic<L> {

    @com.intellij.util.messages.Topic.AppLevel
    public static Topic<AllQuestionNotifier> AllQuestionNotifier = new Topic<>("AllQuestionNotifier");
    @com.intellij.util.messages.Topic.AppLevel
    public static Topic<ConfigNotifier> ConfigNotifier = new Topic<>("ConfigNotifier");
    @com.intellij.util.messages.Topic.AppLevel
    public static Topic<LoginNotifier> LoginNotifier = new Topic<>("LoginNotifier");
    @com.intellij.util.messages.Topic.AppLevel
    public static Topic<QuestionStatusNotifier> QuestionStatusNotifier = new Topic<>("QuestionStatusNotifier");
    @com.intellij.util.messages.Topic.AppLevel
    public static Topic<QuestionSubmitNotifier> QuestionSubmitNotifier = new Topic<>("QuestionSubmitNotifier");
    private String name;

    private Topic(String name) {
        this.name = name;
    }

}
