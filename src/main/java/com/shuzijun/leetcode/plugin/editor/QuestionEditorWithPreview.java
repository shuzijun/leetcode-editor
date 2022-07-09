package com.shuzijun.leetcode.plugin.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.util.Key;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author shuzijun
 */
public class QuestionEditorWithPreview extends TextEditorWithPreview {
    public static final Key<QuestionEditorWithPreview> PARENT_SPLIT_EDITOR_KEY = Key.create(PluginConstant.PLUGIN_ID + "Question Split");

    public QuestionEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview) {
        super(editor, preview, "Question Editor", Layout.SHOW_EDITOR_AND_PREVIEW);
        editor.putUserData(PARENT_SPLIT_EDITOR_KEY, this);
        preview.putUserData(PARENT_SPLIT_EDITOR_KEY, this);
    }

    @Nullable
    protected ActionGroup createLeftToolbarActionGroup() {
        if (!PersistentConfig.getInstance().getInitConfig().isLeftQuestionEditor()) {
            return (ActionGroup) ActionManager.getInstance().getAction(PluginConstant.LEETCODE_EDITOR_GROUP);
        } else {
            return null;
        }

    }

    @Nullable
    protected ActionGroup createRightToolbarActionGroup() {
        if (PersistentConfig.getInstance().getInitConfig().isLeftQuestionEditor()) {
            return (ActionGroup) ActionManager.getInstance().getAction(PluginConstant.LEETCODE_EDITOR_GROUP);
        } else {
            return null;
        }
    }

    @NotNull
    protected ActionGroup createViewActionGroup() {
        if (PersistentConfig.getInstance().getInitConfig().isLeftQuestionEditor()) {
            return new DefaultActionGroup(
                    getShowEditorAndPreviewAction(),
                    getShowPreviewAction()
            );
        } else {
            return new DefaultActionGroup(
                    getShowEditorAction(),
                    getShowEditorAndPreviewAction()
            );
        }


    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return getTextEditor().getBackgroundHighlighter();
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return getTextEditor().getCurrentLocation();
    }

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return getTextEditor().getStructureViewBuilder();
    }


    @NotNull
    public TextEditor getTextEditor() {
        if (PersistentConfig.getInstance().getInitConfig().isLeftQuestionEditor()) {
            if (((TextEditor) myPreview).getEditor() == null) {
                return myEditor;
            }
            return (TextEditor) myPreview;
        } else {
            if ((myEditor).getEditor() == null) {
                return (TextEditor) myPreview;
            }
            return myEditor;
        }

    }

    public FileEditor getPreviewEditor() {
        return myPreview == getTextEditor() ? myEditor : myPreview;
    }
}
