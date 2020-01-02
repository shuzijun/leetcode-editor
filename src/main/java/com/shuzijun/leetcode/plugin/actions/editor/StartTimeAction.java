package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.shuzijun.leetcode.plugin.actions.AbstractAsynAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.timer.TimerBarWidget;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

/**
 * @author shuzijun
 */
public class StartTimeAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config) {
        VirtualFile vf = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(anActionEvent.getProject()).getEditor(vf.getPath());
        if (leetcodeEditor == null) {
            return;
        }
        Question question = ViewManager.getQuestionById(leetcodeEditor.getQuestionId(), anActionEvent.getProject());
        if (question == null) {
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("tree.null"));
            return;
        }
        TimerBarWidget timerBarWidget = (TimerBarWidget) WindowManager.getInstance().getStatusBar(anActionEvent.getProject()).getWidget(TimerBarWidget.ID);
        timerBarWidget.startTimer(question.getTitle());
    }
}
