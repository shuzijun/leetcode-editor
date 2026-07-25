package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/** Small bridge for blocking work that must publish its result back to Swing. */
public final class AsyncUiUtils {
    private AsyncUiUtils() {
    }

    public static <T> void load(Project project, Disposable owner, Callable<T> background,
                                BiConsumer<T, Throwable> uiConsumer) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (project.isDisposed() || Disposer.isDisposed(owner)) {
                return;
            }
            T result = null;
            Throwable error = null;
            try {
                result = background.call();
            } catch (Exception exception) {
                error = exception;
            }
            T finalResult = result;
            Throwable finalError = error;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed() && !Disposer.isDisposed(owner)) {
                    uiConsumer.accept(finalResult, finalError);
                }
            });
        });
    }
}
