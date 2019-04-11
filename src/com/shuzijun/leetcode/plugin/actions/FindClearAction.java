package com.shuzijun.leetcode.plugin.actions;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class FindClearAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (tree == null) {
            return;
        }
        ViewManager.clearFilter();
        ViewManager.updata(tree);
    }
}
