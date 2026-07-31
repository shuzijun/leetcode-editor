package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvergeProviderTest {

    @Test
    public void combinesProviderIdsAndHidesDefaultEditor() {
        FileEditorProvider[] providers = {
                new TestFileEditorProvider("text"),
                new TestFileEditorProvider("preview")
        };

        ConvergeProvider provider = new ConvergeProvider(providers, new String[]{"Code", "Content"});

        assertEquals("tab-provider[text;preview]", provider.getEditorTypeId());
        assertEquals(FileEditorPolicy.HIDE_DEFAULT_EDITOR, provider.getPolicy());
    }

    private static class TestFileEditorProvider implements FileEditorProvider {
        private final String editorTypeId;

        private TestFileEditorProvider(String editorTypeId) {
            this.editorTypeId = editorTypeId;
        }

        @Override
        public String getEditorTypeId() {
            return editorTypeId;
        }

        @Override
        public com.intellij.openapi.fileEditor.FileEditor createEditor(
                com.intellij.openapi.project.Project project,
                com.intellij.openapi.vfs.VirtualFile file) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public boolean accept(com.intellij.openapi.project.Project project,
                              com.intellij.openapi.vfs.VirtualFile file) {
            return true;
        }

        @Override
        public FileEditorPolicy getPolicy() {
            return FileEditorPolicy.NONE;
        }
    }
}
