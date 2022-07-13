package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public abstract class AbstractAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("warning", PropertiesUtils.getInfo("config.first"));
            ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(), PluginConstant.APPLICATION_CONFIGURABLE_DISPLAY_NAME);
            return;
        } else if (StringUtils.isBlank(config.getId())) {
            config.setId(MTAUtils.getI(""));
            PersistentConfig.getInstance().setInitConfig(config);
        }

        try {
            MTAUtils.click(anActionEvent.getActionManager().getId(this), config);
            UpdateUtils.examine(config, anActionEvent.getProject());
        } catch (Exception e) {
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(),anActionEvent.getActionManager().getId(this),false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                actionPerformed(anActionEvent, config);
            }
        });

    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Config config);
}
