package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.Question;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;

/**
 * @author shuzijun
 */
public class RunCodeAction extends AbstractEditAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getCodeService().RunCodeCode(question.getTitleSlug());
    }
}
