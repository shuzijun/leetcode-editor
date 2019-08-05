package com.shuzijun.leetcode.plugin.actions;

import com.alibaba.fastjson.JSON;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class OpenInLeetCodeAction extends AbstractAction {
    @Override public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();
        BrowserUtil.browse(URLUtils.getLeetcodeUrl()+"/problems/"+question.getTitleSlug());
    }
}
