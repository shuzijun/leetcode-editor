package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//https://youtrack.jetbrains.com/issue/IDEA-302416
public class FileEditorProviderReflection {
    public static FileEditorProvider[] getProviders(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        try {
            Method getProvidersMethod = ReflectionUtil.getMethod(
                    FileEditorProviderManager.class,
                    "getProviders",
                    Project.class, VirtualFile.class
            );
            if (getProvidersMethod != null) {
                getProvidersMethod.setAccessible(true);
                // NOTE: Have to get a reference to FileEditorProviderManager this way instead of via getInstance() to
                // avoid a different IncompatibleClassChangeError in pre-2022.3 vs. 2022.3+
                FileEditorProviderManager fileEditorProviderManager = ApplicationManager.getApplication().getService(FileEditorProviderManager.class);
                if (fileEditorProviderManager != null) {
                    Object result = getProvidersMethod.invoke(fileEditorProviderManager, project, virtualFile);
                    if (result instanceof FileEditorProvider[]) {
                        return (FileEditorProvider[]) result;
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogUtils.LOG.warn("Failed to get file editor providers for project '" + project.getName() + "', file '" + virtualFile.getPath() + "'.", e);
        }

        return new FileEditorProvider[0];
    }
}
