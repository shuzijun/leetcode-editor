package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.plugin.editor.converge.NoteProvider;
import com.shuzijun.leetcode.plugin.spi.EditorTabContribution;
import org.jetbrains.annotations.NotNull;

public final class DefaultNoteEditorTabContribution implements EditorTabContribution {

    @Override
    public @NotNull String getId() {
        return "note";
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public @NotNull String getName() {
        return "Note";
    }

    @Override
    public @NotNull FileEditorProvider createEditorProvider() {
        return new NoteProvider();
    }
}
