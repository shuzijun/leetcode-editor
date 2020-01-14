package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractTimeAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.timer.TimerBarWidget;

/**
 * @author shuzijun
 */
public class StopTimeAction extends AbstractTimeAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config, TimerBarWidget timerBarWidget, Question question) {
        timerBarWidget.stopTimer();
    }
}
