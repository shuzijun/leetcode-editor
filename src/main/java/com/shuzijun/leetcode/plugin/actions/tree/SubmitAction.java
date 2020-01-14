package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class SubmitAction extends AbstractTreeAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config, JTree tree, Question question) {
        CodeManager.SubmitCode(question, anActionEvent.getProject());
    }
}
