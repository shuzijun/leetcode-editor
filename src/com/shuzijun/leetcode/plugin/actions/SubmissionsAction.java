    package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.shuzijun.leetcode.plugin.setting.SettingConfigurable;

/**
 * @author shuzijun
 */
public class SubmissionsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(),SettingConfigurable.DISPLAY_NAME);
    }
}
