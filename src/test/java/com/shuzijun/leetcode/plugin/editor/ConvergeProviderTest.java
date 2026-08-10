package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

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

    @Test
    public void rejectsMismatchedProvidersAndNames() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ConvergeProvider(
                        new FileEditorProvider[]{new TestFileEditorProvider("text")},
                        new String[]{"Code", "Content"})
        );

        assertEquals("Editor providers and names must have the same size", exception.getMessage());
    }

    @Test
    public void buildsEveryConfiguredEditorInOrderWithMatchingNames() {
        AtomicInteger buildOrder = new AtomicInteger();
        com.intellij.openapi.fileEditor.FileEditor firstEditor = fileEditor();
        com.intellij.openapi.fileEditor.FileEditor secondEditor = fileEditor();
        TestFileEditorProvider firstProvider =
                new TestFileEditorProvider("content", firstEditor, buildOrder, 0);
        TestFileEditorProvider secondProvider =
                new TestFileEditorProvider("note", secondEditor, buildOrder, 1);
        RecordingConvergeProvider provider = new RecordingConvergeProvider(
                new FileEditorProvider[]{firstProvider, secondProvider},
                new String[]{"Content", "Note"});

        com.intellij.openapi.fileEditor.FileEditor result = provider
                .createEditorAsync(proxy(Project.class), new LightVirtualFile("answer.java"))
                .build();

        assertSame(provider.resultEditor, result);
        assertArrayEquals(new String[]{"Content", "Note"}, provider.recordedNames);
        assertArrayEquals(new Object[]{firstEditor, secondEditor}, provider.recordedEditors);
        assertEquals(2, buildOrder.get());
    }

    @Test
    public void fallsBackToCreateEditorWhenAsyncBuilderIsNotOverridden() {
        com.intellij.openapi.fileEditor.FileEditor expected = fileEditor();
        DefaultAsyncProvider provider = new DefaultAsyncProvider(expected);

        com.intellij.openapi.fileEditor.FileEditor actual = ConvergeProvider
                .getBuilderFromEditorProvider(
                        provider,
                        proxy(Project.class),
                        new LightVirtualFile("answer.java"))
                .build();

        assertSame(expected, actual);
        assertEquals(1, provider.createEditorCalls.get());
    }

    private static class TestFileEditorProvider implements FileEditorProvider {
        private final String editorTypeId;
        private final com.intellij.openapi.fileEditor.FileEditor editor;
        private final AtomicInteger buildOrder;
        private final int expectedOrder;

        private TestFileEditorProvider(String editorTypeId) {
            this(editorTypeId, null, null, -1);
        }

        private TestFileEditorProvider(
                String editorTypeId,
                com.intellij.openapi.fileEditor.FileEditor editor,
                AtomicInteger buildOrder,
                int expectedOrder) {
            this.editorTypeId = editorTypeId;
            this.editor = editor;
            this.buildOrder = buildOrder;
            this.expectedOrder = expectedOrder;
        }

        @Override
        public String getEditorTypeId() {
            return editorTypeId;
        }

        @Override
        public com.intellij.openapi.fileEditor.FileEditor createEditor(
                com.intellij.openapi.project.Project project,
                com.intellij.openapi.vfs.VirtualFile file) {
            if (buildOrder != null) {
                assertEquals(expectedOrder, buildOrder.getAndIncrement());
                return editor;
            }
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

    private static class RecordingConvergeProvider extends ConvergeProvider {
        private final TextEditor resultEditor = textEditor();
        private com.intellij.openapi.fileEditor.FileEditor[] recordedEditors;
        private String[] recordedNames;

        private RecordingConvergeProvider(FileEditorProvider[] editorProviders, String[] names) {
            super(editorProviders, names);
        }

        @Override
        protected TextEditor createSplitEditor(
                com.intellij.openapi.fileEditor.FileEditor[] fileEditors,
                String[] names,
                Project project,
                VirtualFile file) {
            recordedEditors = Arrays.copyOf(fileEditors, fileEditors.length);
            recordedNames = Arrays.copyOf(names, names.length);
            return resultEditor;
        }
    }

    private static class DefaultAsyncProvider implements AsyncFileEditorProvider {
        private final com.intellij.openapi.fileEditor.FileEditor editor;
        private final AtomicInteger createEditorCalls = new AtomicInteger();

        private DefaultAsyncProvider(com.intellij.openapi.fileEditor.FileEditor editor) {
            this.editor = editor;
        }

        @Override
        public com.intellij.openapi.fileEditor.FileEditor createEditor(Project project, VirtualFile file) {
            createEditorCalls.incrementAndGet();
            return editor;
        }

        @Override
        public boolean accept(Project project, VirtualFile file) {
            return true;
        }

        @Override
        public String getEditorTypeId() {
            return "default-async";
        }

        @Override
        public FileEditorPolicy getPolicy() {
            return FileEditorPolicy.NONE;
        }
    }

    private static com.intellij.openapi.fileEditor.FileEditor fileEditor() {
        return proxy(com.intellij.openapi.fileEditor.FileEditor.class);
    }

    private static TextEditor textEditor() {
        return proxy(TextEditor.class);
    }

    private static <T> T proxy(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, arguments) -> {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    return null;
                }));
    }
}
