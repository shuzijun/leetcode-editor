package com.shuzijun.leetcode.plugin.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.URLUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author zzdcon
 */
public class OpenInWebAction extends AbstractAction {
    @Override public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();
        BrowserUtil.browse(URLUtils.getLeetcodeProblems()+question.getTitleSlug());
    }
}
