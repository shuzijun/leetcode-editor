package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

/**
 * @author hongjinfeng
 * @date 2021/5/20 12:20 下午
 */
public class SortActionGroup extends DefaultActionGroup {

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(true);
    }
}
