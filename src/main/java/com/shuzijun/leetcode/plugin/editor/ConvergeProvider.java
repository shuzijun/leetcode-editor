package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.extension.ConvergeEditorFactory;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author shuzijun
 */
public class ConvergeProvider implements AsyncFileEditorProvider, DumbAware {

    private static final ExtensionPointName<ConvergeEditorFactory> EXTENSION_CONVERGE_EDITOR = ExtensionPointName.create(PluginConstant.EXTENSION_CONVERGE_EDITOR);

    private String myEditorTypeId = "tab-provider-ConvergeProvider";

    public ConvergeProvider() {
    }

    @NotNull
    public static Builder getBuilderFromEditorProvider(@NotNull final FileEditorProvider provider, @NotNull final Project project, @NotNull final VirtualFile file) {
        if (provider instanceof AsyncFileEditorProvider) {
            return ((AsyncFileEditorProvider) provider).createEditorAsync(project, file);
        } else {
            return new Builder() {
                @Override
                public FileEditor build() {
                    return provider.createEditor(project, file);
                }
            };
        }
    }

    @NotNull
    @Override
    public AsyncFileEditorProvider.Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
        RepositoryService repositoryService = RepositoryServiceImpl.getInstance(project);
        List<ConvergeEditorFactory> convergeEditorFactory = EXTENSION_CONVERGE_EDITOR.getExtensionList();

        final Builder[] builders = new Builder[convergeEditorFactory.size()];
        final String[] names = new String[convergeEditorFactory.size()];
        for (int i = 0; i < convergeEditorFactory.size(); i++) {
            builders[i] = getBuilderFromEditorProvider(convergeEditorFactory.get(i).createEditorProvider(repositoryService), project, file);
            names[i] = convergeEditorFactory.get(i).getName();
        }


        return new Builder() {
            @Override
            public TextEditor build() {
                FileEditor[] fileEditors = new FileEditor[convergeEditorFactory.size()];
                for (int i = 0; i < builders.length; i++) {
                    fileEditors[i] = builders[i].build();
                }
                return createSplitEditor(fileEditors, names, project, file);
            }
        };
    }

    protected TextEditor createSplitEditor(@NotNull FileEditor[] fileEditors, String[] names, Project project, VirtualFile file) {
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
}