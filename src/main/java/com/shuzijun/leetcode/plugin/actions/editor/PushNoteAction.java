package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.manager.NoteManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;

/**
 * @author shuzijun
 */
public class PushNoteAction extends AbstractEditAction {

    @Override
    public void actionPerformed(
            AnActionEvent anActionEvent,
            Config config,
            LeetcodeEditor leetcodeEditor,
            Question question
    ) {
        NoteManager.push(question.getTitleSlug(), anActionEvent.getProject());
        if (config.getConvergeEditor()) {
            openConvergeEditor(
                    anActionEvent,
                    new ConvergePreview.TabSelectFileEditorState("Note", "refresh")
            );
        }
    }
}
