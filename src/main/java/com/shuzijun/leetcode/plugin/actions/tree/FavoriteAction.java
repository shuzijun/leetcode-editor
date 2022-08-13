package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.extension.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.QuestionView;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
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
        Question cacheQuestion = RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getQuestionService().getQuestionByTitleSlug(questionView.getTitleSlug());
        if (cacheQuestion == null) {
            return false;
        }
        return tag.getQuestions().contains(cacheQuestion.getQuestionId());
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        NavigatorAction<QuestionView> navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        QuestionView questionView = navigatorAction.getSelectedRowData();
        if (questionView == null) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), PluginConstant.PLUGIN_NAME + ".favorite", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                RepositoryService repositoryService = RepositoryServiceImpl.getInstance(anActionEvent.getProject());
                if (b) {
                    repositoryService.getFavoriteService().addQuestionToFavorite(tag, questionView.getTitleSlug());
                } else {
                    repositoryService.getFavoriteService().removeQuestionFromFavorite(tag, questionView.getTitleSlug());
                }
            }
        });

    }
}
