package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.SettingConfigurable;
import com.shuzijun.leetcode.plugin.utils.MTAUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.UpdateUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author shuzijun
 */
public abstract class AbstractAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("config.first"));
            ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(), SettingConfigurable.DISPLAY_NAME);
            return;
        } else if (StringUtils.isBlank(config.getId())) {
            config.setId(MTAUtils.getI(""));
            PersistentConfig.getInstance().setInitConfig(config);
        }

        try {
            MTAUtils.click(anActionEvent.getActionManager().getId(this),config);
            UpdateUtils.examine(config);
        }catch (Exception e){
        }



        actionPerformed(anActionEvent, config);
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Config config);
}
