package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.ConvergeFileEditorState;
import com.shuzijun.leetcode.platform.model.Question;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;

/**
 * @author shuzijun
 */
public class PullNoteAction extends AbstractEditAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        RepositoryService repositoryService = RepositoryServiceImpl.getInstance(anActionEvent.getProject());
        repositoryService.getNoteService().pull(question.getTitleSlug());
        if (config.getConvergeEditor() && openConvergeEditor(anActionEvent, new ConvergeFileEditorState.TabSelectFileEditorState("Note"))) {
            return;
        }
        repositoryService.getNoteService().show(question.getTitleSlug(), true);
    }
}
