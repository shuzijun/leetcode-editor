package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class RefreshAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        ViewManager.loadServiceData(tree, anActionEvent.getProject());

    }



}
