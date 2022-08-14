package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.*;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.dialog.SolutionPanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author shuzijun
 */
public class OpenSolutionAction extends AbstractEditAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        VirtualFile vf = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (vf == null) {
            return;
        }
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(anActionEvent.getProject()).getEditor(vf.getPath());
        if (leetcodeEditor == null || StringUtils.isBlank(leetcodeEditor.getTitleSlug()) || !URLUtils.equalsHost(leetcodeEditor.getHost())) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        Question question = RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getQuestionService().getQuestionByTitleSlug(leetcodeEditor.getTitleSlug());
        if (question != null) {
            anActionEvent.getPresentation().setEnabled(!Constant.ARTICLE_LIVE_NONE.equals(question.getArticleLive()));
        } else {
            anActionEvent.getPresentation().setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        Project project = anActionEvent.getProject();
        RepositoryService repositoryService = RepositoryServiceImpl.getInstance(project);
        if (Constant.ARTICLE_LIVE_ONE.equals(question.getArticleLive())) {
            if (config.getConvergeEditor() && openConvergeEditor(anActionEvent, new ConvergeFileEditorState.TabSelectFileEditorState("Solution"))) {
                return;
            }
            repositoryService.getArticleService().openArticle(question.getTitleSlug(), question.getArticleSlug(), true);
        } else if (Constant.ARTICLE_LIVE_LIST.equals(question.getArticleLive())) {
            List<Solution> solutionList = repositoryService.getArticleService().getSolutionList(question.getTitleSlug());
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
        if (solution != null) {
            if (config.getConvergeEditor() && openConvergeEditor(anActionEvent, new ConvergeFileEditorState.TabSelectFileEditorState("Solution", solution.getSlug()))) {
                return;
            }
            ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), anActionEvent.getActionManager().getId(this), false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    question.setArticleSlug(solution.getSlug());
                    RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getArticleService().openArticle(question.getTitleSlug(), question.getArticleSlug(), true);
                }
            });

        }
    }
}
