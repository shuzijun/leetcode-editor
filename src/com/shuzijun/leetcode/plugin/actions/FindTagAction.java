package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class FindTagAction extends ToggleAction {

    private Tag tag;

    public FindTagAction(@Nullable String text, Tag tag) {
        super(text);
        this.tag = tag;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return tag.isSelect();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        tag.setSelect(b);
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (tree == null) {
            return;
        }
        ViewManager.updata(tree);
    }


}
