package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.shuzijun.leetcode.plugin.manager.FavoriteManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author shuzijun
 */
public class FavoriteAction extends ToggleAction {

    private Tag tag;

    public FavoriteAction(@Nullable String text, Tag tag) {
        super(text);
        this.tag = tag;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {

        NavigatorTable navigatorTable = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        final Question question = navigatorTable.getSelectedRowData();
        if (question == null) {
            return false;
        }
        Question cacheQuestion = ViewManager.getCaCheQuestionByTitleSlug(question.getTitleSlug(), null, anActionEvent.getProject());
        if (cacheQuestion == null) {
            try {
                cacheQuestion = ApplicationManager.getApplication().executeOnPooledThread(() -> ViewManager.getQuestionByTitleSlug(question.getTitleSlug(), null, anActionEvent.getProject())).get();
            } catch (Exception e) {
            }
        }
        if (cacheQuestion == null) {
            return false;
        }
        return tag.getQuestions().contains(cacheQuestion.getQuestionId());
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        NavigatorTable navigatorTable = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        Question question = navigatorTable.getSelectedRowData();
        if (question == null) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), PluginConstant.PLUGIN_NAME + ".favorite", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                if (b) {
                    FavoriteManager.addQuestionToFavorite(tag, question, anActionEvent.getProject());
                } else {
                    FavoriteManager.removeQuestionFromFavorite(tag, question, anActionEvent.getProject());
                }
            }
        });

    }
}
