package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import org.jetbrains.annotations.NotNull;

public interface EditorTabContribution extends OrderedContribution {

    ExtensionPointName<EditorTabContribution> EP_NAME =
            ExtensionPointName.create("leetcode-editor.editorTabContribution");

    @NotNull String getName();

    @NotNull FileEditorProvider createEditorProvider();
}
