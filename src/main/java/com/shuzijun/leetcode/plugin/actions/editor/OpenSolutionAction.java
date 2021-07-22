package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.manager.ArticleManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.window.SolutionPanel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
        if (leetcodeEditor == null) {
            return;
        }
        Question question = ViewManager.getDumbQuestionById(leetcodeEditor.getQuestionId(), anActionEvent.getProject());
        if (question == null) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        anActionEvent.getPresentation().setEnabled(!Constant.ARTICLE_LIVE_NONE.equals(question.getArticleLive()));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
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
