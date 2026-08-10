package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.plugin.editor.converge.ContentProvider;
import com.shuzijun.leetcode.plugin.spi.EditorTabContribution;
import org.jetbrains.annotations.NotNull;

public final class DefaultContentEditorTabContribution implements EditorTabContribution {

    @Override
    public @NotNull String getId() {
        return "content";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public @NotNull String getName() {
        return "Content";
    }

    @Override
    public @NotNull FileEditorProvider createEditorProvider() {
        return new ContentProvider();
    }
}
