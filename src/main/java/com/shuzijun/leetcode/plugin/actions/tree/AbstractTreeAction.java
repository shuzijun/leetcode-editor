package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import javax.swing.*;

/**
 * @author shuzijun
 */
public abstract class AbstractTreeAction extends AbstractAction {

    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (tree == null) {
            return;
        }
        Question question = ViewManager.getTreeQuestion(tree, anActionEvent.getProject());
        if (question == null) {
            return;
        }
        actionPerformed(anActionEvent, config, tree, question);
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Config config, JTree tree, Question question);
}
