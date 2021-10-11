package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.NoteManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;

/**
 * @author shuzijun
 */
public class ShowNoteAction  extends  AbstractTreeAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, NavigatorTable navigatorTable, Question question) {
        NoteManager.show(question,anActionEvent.getProject());
    }
}
