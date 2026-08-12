package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.manager.FavoriteManager;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author shuzijun
 */
public class FavoriteAction extends ToggleAction implements DumbAware {

    private Tag tag;

    public FavoriteAction(@Nullable String text, Tag tag) {
        super(text);
        this.tag = tag;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {

        NavigatorAction<QuestionView> navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        final QuestionView questionView = navigatorAction.getSelectedRowData();
        if (questionView == null) {
            return false;
        }
        return isQuestionFavorite(tag, questionView.getFrontendQuestionId());
    }

    static boolean isQuestionFavorite(Tag tag, String frontendQuestionId) {
        return tag.getQuestions().contains(frontendQuestionId);
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        NavigatorAction<QuestionView> navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        QuestionView questionView = navigatorAction.getSelectedRowData();
        if (questionView == null) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), ProductProfiles.current().pluginName() + ".favorite", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                boolean updated = b
                        ? FavoriteManager.addQuestionToFavorite(tag, questionView.getTitleSlug(), anActionEvent.getProject())
                        : FavoriteManager.removeQuestionFromFavorite(tag, questionView.getTitleSlug(), anActionEvent.getProject());
                if (updated) {
                    ApplicationManager.getApplication().invokeLater(() -> ActivityTracker.getInstance().inc());
                }
            }
        });

    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
