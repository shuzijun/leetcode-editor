package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.ArticleManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Solution;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import com.shuzijun.leetcode.plugin.window.SolutionPanel;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author shuzijun
 */
public class OpenSolutionAction extends AbstractTreeAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        NavigatorTable navigatorTable = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        if (navigatorTable == null) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        Question question = navigatorTable.getSelectedRowData();
        if (question == null) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        if (Constant.ARTICLE_LIVE_NONE.equals(question.getArticleLive())) {
            anActionEvent.getPresentation().setEnabled(false);
        } else {
            anActionEvent.getPresentation().setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, NavigatorTable navigatorTable, Question question) {
        Project project = anActionEvent.getProject();
        if (Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
            ArticleManager.openArticle(question, project);
        } else if (Constant.ARTICLE_LIVE_LIST.equals(question.getArticleLive())) {
            List<Solution> solutionList = ArticleManager.getSolutionList(question, anActionEvent.getProject());
            if (solutionList.isEmpty()) {
                return;
            }
            AtomicReference<Solution> solution = new AtomicReference<>();
            ApplicationManager.getApplication().invokeAndWait(() -> {
                SolutionPanel.TableModel tableModel = new SolutionPanel.TableModel(solutionList);
                SolutionPanel dialog = new SolutionPanel(anActionEvent.getProject(), tableModel);
                dialog.setTitle(question.getFormTitle() + " Solutions");

                if (dialog.showAndGet()) {
                    solution.set(solutionList.get(dialog.getSelectedRow()));
                }
            });
            if(solution.get() !=null){
                question.setArticleSlug(solution.get().getSlug());
                ArticleManager.openArticle(question, project);
            }

        }

    }
}
