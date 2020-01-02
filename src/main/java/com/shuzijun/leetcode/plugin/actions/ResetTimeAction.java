package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.WindowManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.timer.TimerBarWidget;

/**
 * @author shuzijun
 */
public class ResetTimeAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config) {
        TimerBarWidget timerBarWidget = (TimerBarWidget) WindowManager.getInstance().getStatusBar(anActionEvent.getProject()).getWidget(TimerBarWidget.ID);
        timerBarWidget.reset();
    }
}
