package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.Question;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;

/**
 * @author shuzijun
 */
public class ShowNoteAction extends AbstractTreeAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getNoteService().show(question.getTitleSlug(), true);
    }
}
