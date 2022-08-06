package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author shuzijun
 */
public class QuestionEditorTabTitleProvider implements EditorTabTitleProvider {
    @Override
    public @NlsContexts.TabTitle @Nullable String getEditorTabTitle(@NotNull Project project, @NotNull VirtualFile file) {
        try {
            Config config = PersistentConfig.getInstance().getInitConfig();
            if (config == null || !config.isShowQuestionEditor() || !config.isShowQuestionEditorSign()) {
                return null;
            }
            LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(project).getEditor(file.getPath(), config.getUrl());
            if (leetcodeEditor == null || StringUtils.isBlank(leetcodeEditor.getContentPath())) {
                return null;
            } else {
                Question question = QuestionManager.getQuestionByTitleSlug(leetcodeEditor.getTitleSlug(), project);
                if (question == null) {
                    return null;
                } else {
                    return question.getFormTitle();
                }
            }
        } catch (Throwable e) {
            LogUtils.LOG.error("QuestionEditorIconProvider -> patchIcon", e);
            return null;
        }
    }
}
