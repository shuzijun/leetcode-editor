package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class RefreshAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config) {

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        ViewManager.loadServiceData(tree);

    }



}
