package com.shuzijun.leetcode.plugin.actions.toolbar;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

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
        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (tree == null) {
            return;
        }
        ViewManager.update(tree);
    }
}
