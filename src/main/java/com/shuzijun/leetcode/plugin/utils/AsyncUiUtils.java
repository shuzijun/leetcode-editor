package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.concurrency.AppExecutorUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/** Small bridge for blocking work that must publish its result back to Swing. */
public final class AsyncUiUtils {
    private AsyncUiUtils() {
    }

    public static <T> void load(Project project, Disposable owner, Callable<T> background,
                                BiConsumer<T, Throwable> uiConsumer) {
        AtomicBoolean disposed = new AtomicBoolean();
        Disposer.register(owner, () -> disposed.set(true));
        AppExecutorUtil.getAppExecutorService().execute(() -> {
            if (project.isDisposed() || disposed.get()) {
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
                if (!project.isDisposed() && !disposed.get()) {
                    uiConsumer.accept(finalResult, finalError);
                }
            }, ignored -> project.isDisposed() || disposed.get());
        });
    }
}
