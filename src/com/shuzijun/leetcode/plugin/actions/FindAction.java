package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class FindAction extends ToggleAction {


    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        JPanel panel = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TERRFIND);
        return panel.isVisible();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        JPanel panel = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TERRFIND);
        panel.setVisible(b);
    }

}
