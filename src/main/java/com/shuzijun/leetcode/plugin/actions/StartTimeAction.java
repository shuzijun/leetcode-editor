package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.WindowManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.timer.TimerBarWidget;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class StartTimeAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config) {
        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        Question question = ViewManager.getTreeQuestion(tree, anActionEvent.getProject());
        if (question == null) {
            return;
        }
        TimerBarWidget timerBarWidget = (TimerBarWidget) WindowManager.getInstance().getStatusBar(anActionEvent.getProject()).getWidget(TimerBarWidget.ID);
        timerBarWidget.startTimer(question.getTitle());
    }
}
