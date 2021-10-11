package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

/**
 * @author shuzijun
 */
public abstract class AbstractTreeAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        NavigatorTable navigatorTable = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (navigatorTable == null) {
            return;
        }
        Question question = navigatorTable.getSelectedRowData();
        if (question == null) {
            return;
        }
        actionPerformed(anActionEvent, config, navigatorTable, question);
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Config config, NavigatorTable navigatorTable, Question question);
}
