package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;

/**
 * @author shuzijun
 */
public class ShowNoteAction extends AbstractEditAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        if (config.getConvergeEditor() && openConvergeEditor(anActionEvent, new ConvergePreview.TabSelectFileEditorState("Note"))) {
            return;
        }
        RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getNoteService().show(question.getTitleSlug(), true);
    }
}
