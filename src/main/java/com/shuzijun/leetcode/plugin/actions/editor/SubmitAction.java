package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;

/**
 * @author shuzijun
 */
public class SubmitAction extends AbstractEditAction {

    @Override
    public void actionPerformed(
            AnActionEvent anActionEvent,
            Config config,
            LeetcodeEditor leetcodeEditor,
            Question question
    ) {
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnumByLangSlug(leetcodeEditor.getLangSlug());
        CodeManager.SubmitCode(question.getTitleSlug(), anActionEvent.getProject(), codeTypeEnum);
    }
}
