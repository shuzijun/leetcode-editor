package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class FindTagAction extends ToggleAction {

    private Tag tag;

    private boolean againLoad = false;

    public FindTagAction(@Nullable String text, Tag tag) {
        super(text);
        this.tag = tag;
    }

    public FindTagAction(@Nullable String text, Tag tag, boolean againLoad) {
        super(text);
        this.tag = tag;
        this.againLoad = againLoad;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return tag.isSelect();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        tag.setSelect(b);
        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (tree == null) {
            return;
        }
        if (againLoad) {
            ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), PluginConstant.PLUGIN_NAME+ "." + tag.getName(), false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    if (b) {
                        ViewManager.loadServiceData(tree, anActionEvent.getProject(), tag.getSlug());
                    } else {
                        ViewManager.loadServiceData(tree, anActionEvent.getProject());
                    }

                }
            });

        } else {
            ViewManager.update(tree);
        }
    }


}
