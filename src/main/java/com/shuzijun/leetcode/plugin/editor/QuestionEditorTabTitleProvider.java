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
            }

            // IDEA calls tab title providers while restoring editors during startup. This
            // callback must stay local: waiting for LeetCode here freezes the whole IDE.
            Question question = QuestionManager.getCachedQuestionByTitleSlug(
                    leetcodeEditor.getTitleSlug(), leetcodeEditor.getHost());
            return resolveLocalTitle(question, file.getNameWithoutExtension());
        } catch (Throwable e) {
            LogUtils.LOG.error("QuestionEditorTabTitleProvider -> getEditorTabTitle", e);
            return null;
        }
    }

    static String resolveLocalTitle(@Nullable Question cachedQuestion, @NotNull String fallbackTitle) {
        return cachedQuestion == null ? fallbackTitle : cachedQuestion.getFormTitle();
    }
}
