package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.model.PluginConstant;

/**
 * @author shuzijun
 */
public class ConfigAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(), PluginConstant.APPLICATION_CONFIGURABLE_DISPLAY_NAME);
    }
}
