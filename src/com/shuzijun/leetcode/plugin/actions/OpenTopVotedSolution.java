package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author rufus
 */
public class OpenTopVotedSolution extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();
        Project project = anActionEvent.getProject();


        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CodeManager.openTopVotedSolution(question, project);
            }
        });
    }
}
