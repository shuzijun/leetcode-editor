package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;

/**
 * @author shuzijun
 */
public class RunCodeAction extends AbstractTreeAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(config.getCodeType());
        CodeManager.RunCodeCode(question.getTitleSlug(), anActionEvent.getProject(), codeTypeEnum);
    }
}
