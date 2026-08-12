package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.WindowManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.timer.TimerBarWidget;

/**
 * @author shuzijun
 */
public class StartTimeAction extends AbstractEditAction {

    @Override
    public void actionPerformed(
            AnActionEvent anActionEvent,
            Config config,
            LeetcodeEditor leetcodeEditor,
            Question question
    ) {
        TimerBarWidget timerBarWidget = (TimerBarWidget) WindowManager.getInstance().getStatusBar(anActionEvent.getProject()).getWidget(TimerBarWidget.ID);
        if (timerBarWidget != null) {
            timerBarWidget.startTimer(question.getTitle());
        } else {
            //For possible reasons, the IDE version is not supported
        }
    }
}
