package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.editor.converge.ContentProvider;
import com.shuzijun.leetcode.plugin.editor.converge.NoteProvider;
import com.shuzijun.leetcode.plugin.editor.converge.SolutionProvider;
import com.shuzijun.leetcode.plugin.editor.converge.SubmissionsProvider;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author shuzijun
 */
public class QuestionEditorProvider extends SplitTextEditorProvider {

    public QuestionEditorProvider() {
        super(new PsiAwareTextEditorProvider(), new ConvergeProvider(new FileEditorProvider[]{new ContentProvider(), new SolutionProvider(), new SubmissionsProvider(), new NoteProvider()}, new String[]{"Content", "Solution", "Submissions", "Note"}));
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        try {
            Config config = PersistentConfig.getInstance().getInitConfig();
            if (config == null || !config.isShowQuestionEditor()) {
                return false;
            }
            LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(project).getEditor(file.getPath());
            if (leetcodeEditor == null || StringUtils.isBlank(leetcodeEditor.getContentPath())) {
                return false;
            }
            File contentFile = new File(leetcodeEditor.getContentPath());
            if (!contentFile.exists()) {
                return false;
            }
        } catch (Throwable e) {
            LogUtils.LOG.error("QuestionEditorProvider -> accept", e);
            return false;
        }
        return this.myFirstProvider.accept(project, file);
    }

    @Override
    public Builder createEditorAsync(@NotNull Project project, @NotNull VirtualFile file) {

        final Builder firstBuilder = getBuilderFromEditorProvider(this.myFirstProvider, project, file);
        final Builder secondBuilder = getBuilderFromEditorProvider(this.mySecondProvider, project, file);
        return new Builder() {
            public FileEditor build() {
                return createSplitEditor(firstBuilder.build(), secondBuilder.build());
            }
        };
    }

    @Override
    protected FileEditor createSplitEditor(@NotNull FileEditor firstEditor, @NotNull FileEditor secondEditor) {

        if (PersistentConfig.getInstance().getInitConfig().isLeftQuestionEditor()) {
            return new QuestionEditorWithPreview((TextEditor) secondEditor, firstEditor);
        } else {
            return new QuestionEditorWithPreview((TextEditor) firstEditor, secondEditor);
        }

        //if (firstEditor instanceof TextEditor && secondEditor instanceof MarkdownSplitEditor) {

        //} else {
        //    throw new IllegalArgumentException("Main editor should be TextEditor");
        //}
    }


}
