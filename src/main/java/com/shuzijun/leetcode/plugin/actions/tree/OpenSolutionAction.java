package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.shuzijun.leetcode.plugin.manager.ArticleManager;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import com.shuzijun.leetcode.plugin.window.dialog.SolutionPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author shuzijun
 */
public class OpenSolutionAction extends AbstractTreeAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        NavigatorAction<QuestionView> navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        if (navigatorAction == null) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        QuestionView questionView = navigatorAction.getSelectedRowData();
        if (questionView == null) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(questionView.getTitleSlug(), anActionEvent.getProject());
        if (question == null) {
            return;
        }
        if (Constant.ARTICLE_LIVE_NONE.equals(question.getArticleLive())) {
            anActionEvent.getPresentation().setEnabled(false);
        } else {
            anActionEvent.getPresentation().setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        Project project = anActionEvent.getProject();
        if (Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
            ArticleManager.openArticle(question.getTitleSlug(), question.getArticleSlug(), project, true);
        } else if (Constant.ARTICLE_LIVE_LIST.equals(question.getArticleLive())) {
            List<Solution> solutionList = ArticleManager.getSolutionList(question.getTitleSlug(), anActionEvent.getProject());
            if (solutionList.isEmpty()) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                SolutionPanel.TableModel tableModel = new SolutionPanel.TableModel(solutionList);
                SolutionPanel dialog = new SolutionPanel(anActionEvent.getProject(), tableModel);
                dialog.setTitle(question.getFormTitle() + " Solutions");
                dialog.addTableMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                            int row = dialog.getSelectedRow();
                            openArticle(anActionEvent, config, question, solutionList, row);
                            dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
                        }
                    }
                });
                dialog.addTableKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                            int row = dialog.getSelectedRow();
                            openArticle(anActionEvent, config, question, solutionList, row);
                            dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
                        }

                    }
                });
                dialog.show();
            });
        }
    }

    private void openArticle(AnActionEvent anActionEvent, Config config, Question question, List<Solution> solutionList, int row) {
        Solution solution = solutionList.get(row);

        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), anActionEvent.getActionManager().getId(this), false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                question.setArticleSlug(solution.getSlug());
                ArticleManager.openArticle(question.getTitleSlug(), question.getArticleSlug(), anActionEvent.getProject(), true);
            }
        });
    }
}
