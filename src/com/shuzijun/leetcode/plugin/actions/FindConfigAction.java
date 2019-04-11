package com.shuzijun.leetcode.plugin.actions;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class FindConfigAction extends CheckboxAction {

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return ViewManager.isIntersection();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        ViewManager.setIntersection(b);
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (tree == null) {
            return;
        }
        ViewManager.updata(tree);
    }
}
