package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.application.LeetCodeApplicationService;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.spi.EditorTabContribution;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shuzijun
 */
public class ConvergeProvider implements AsyncFileEditorProvider, DumbAware {

    @NotNull
    protected final FileEditorProvider[] editorProviders;
    protected final String[] names;
    private final String myEditorTypeId;

    public ConvergeProvider() {
        this(
                LeetCodeApplicationService.getInstance().editorTabs(),
                ProductProfiles.current().convergeEditorTypeId()
        );
    }

    private ConvergeProvider(
            @NotNull List<EditorTabContribution> contributions,
            @Nullable String editorTypeId) {
        this(
                contributions.stream()
                        .map(EditorTabContribution::createEditorProvider)
                        .toArray(FileEditorProvider[]::new),
                contributions.stream()
                        .map(EditorTabContribution::getName)
                        .toArray(String[]::new),
                editorTypeId
        );
    }

    public ConvergeProvider(@NotNull FileEditorProvider[] editorProviders, @NotNull String[] names) {
        this(editorProviders, names, null);
    }

    private ConvergeProvider(
            @NotNull FileEditorProvider[] editorProviders,
            @NotNull String[] names,
            @Nullable String editorTypeId) {
        if (editorProviders.length != names.length) {
            throw new IllegalArgumentException("Editor providers and names must have the same size");
        }
        this.editorProviders = editorProviders;
        this.names = names;
        this.myEditorTypeId = editorTypeId == null
                ? "tab-provider[" + Arrays.stream(editorProviders)
                        .map(FileEditorProvider::getEditorTypeId)
                        .collect(Collectors.joining(";")) + "]"
                : editorTypeId;
    }


    @NotNull
    @Override
    public AsyncFileEditorProvider.Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
        final Builder[] builders = new Builder[editorProviders.length];
        for (int i = 0; i < editorProviders.length; i++) {
            builders[i] = getBuilderFromEditorProvider(editorProviders[i], project, file);
        }
        return new Builder() {
            @Override
            public TextEditor build() {
                FileEditor[] fileEditors = new FileEditor[builders.length];
                for (int i = 0; i < builders.length; i++) {
                    fileEditors[i] = builders[i].build();
                }
                return createSplitEditor(fileEditors, project, file);
            }
        };
    }

    protected TextEditor createSplitEditor(@NotNull FileEditor[] fileEditors, Project project, VirtualFile file) {
        return createSplitEditor(fileEditors, names, project, file);
    }

    protected TextEditor createSplitEditor(
            @NotNull FileEditor[] fileEditors,
            String[] names,
            Project project,
            VirtualFile file) {
        return new ConvergePreview(fileEditors, names, project, file);
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return true;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return createEditorAsync(project, file).build();
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return myEditorTypeId;
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @NotNull
    public static Builder getBuilderFromEditorProvider(@NotNull final FileEditorProvider provider, @NotNull final Project project, @NotNull final VirtualFile file) {
        if (hasAsyncBuilder(provider)) {
            return ((AsyncFileEditorProvider) provider).createEditorAsync(project, file);
        }
        return new Builder() {
            @Override
            public FileEditor build() {
                return provider.createEditor(project, file);
            }
        };
    }

    private static boolean hasAsyncBuilder(@NotNull FileEditorProvider provider) {
        if (!(provider instanceof AsyncFileEditorProvider)) {
            return false;
        }
        try {
            return provider.getClass()
                    .getMethod("createEditorAsync", Project.class, VirtualFile.class)
                    .getDeclaringClass() != AsyncFileEditorProvider.class;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }
}
