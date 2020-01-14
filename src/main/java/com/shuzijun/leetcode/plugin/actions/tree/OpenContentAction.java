package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class OpenContentAction extends AbstractTreeAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, JTree tree, Question question) {
        Project project = anActionEvent.getProject();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CodeManager.openContent(question, project);
            }
        });
    }
}
