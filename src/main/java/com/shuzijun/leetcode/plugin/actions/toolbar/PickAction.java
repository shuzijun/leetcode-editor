package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

/**
 * @author shuzijun
 */
public class PickAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(config.getCodeType());
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }
        ViewManager.pick(codeTypeEnum, anActionEvent.getProject());
    }
}
