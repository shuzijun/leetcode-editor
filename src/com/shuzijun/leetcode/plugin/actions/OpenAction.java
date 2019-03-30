package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author shuzijun
 */
public class OpenAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();

        Project project = anActionEvent.getProject();

        CodeManager.openCode(question, project);
    }
}
