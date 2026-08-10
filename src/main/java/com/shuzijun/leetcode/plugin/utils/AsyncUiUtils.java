package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.concurrency.AppExecutorUtil;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.BiConsumer;

/** Small bridge for blocking work that must publish its result back to Swing. */
public final class AsyncUiUtils {
    private static final ConcurrentMap<Operation, AsyncTaskHandle> OPERATIONS = new ConcurrentHashMap<>();

    private AsyncUiUtils() {
    }

    public static <T> AsyncTaskHandle load(Project project, Disposable owner, Callable<T> background,
                                           BiConsumer<T, Throwable> uiConsumer) {
        return load(project, owner, null, background, uiConsumer);
    }

    public static <T> AsyncTaskHandle load(Project project, Disposable owner, Object operationKey,
                                           Callable<T> background, BiConsumer<T, Throwable> uiConsumer) {
        return load(project, owner, operationKey, background, uiConsumer,
                AppExecutorUtil.getAppExecutorService(),
                (runnable, expired) -> ApplicationManager.getApplication().invokeLater(runnable, ignored -> expired.getAsBoolean()));
    }

    static <T> AsyncTaskHandle load(Project project, Disposable owner, Object operationKey,
                                    Callable<T> background, BiConsumer<T, Throwable> uiConsumer,
                                    ExecutorService executor, UiScheduler uiScheduler) {
        Objects.requireNonNull(project, "project");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(background, "background");
        Objects.requireNonNull(uiConsumer, "uiConsumer");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(uiScheduler, "uiScheduler");

        AsyncTaskHandle handle = new AsyncTaskHandle();
        Operation operation = operationKey == null ? null : new Operation(owner, operationKey);
        if (operation != null) {
            AsyncTaskHandle previous = OPERATIONS.put(operation, handle);
            if (previous != null) {
                previous.cancel();
            }
        }

        LifecycleRegistration ownerRegistration = register(owner, handle);
        LifecycleRegistration projectRegistration = owner == project ? null : register(project, handle);
        Completion completion = new Completion(operation, handle, ownerRegistration, projectRegistration);
        handle.onCancellation(completion);
        if (project.isDisposed() || Disposer.isDisposed(owner)) {
            handle.cancel();
            return handle;
        }

        handle.attach(executor.submit(() -> {
            if (isExpired(project, owner, handle)) {
                handle.cancel();
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
            uiScheduler.schedule(() -> {
                try {
                    if (!isExpired(project, owner, handle)) {
                        uiConsumer.accept(finalResult, finalError);
                    }
                } finally {
                    completion.run();
                }
            }, () -> {
                if (isExpired(project, owner, handle)) {
                    handle.cancel();
                    return true;
                }
                return false;
            });
        }));
        return handle;
    }

    private static LifecycleRegistration register(Disposable parent, AsyncTaskHandle handle) {
        LifecycleRegistration registration = new LifecycleRegistration(handle);
        Disposer.register(parent, registration);
        return registration;
    }

    private static boolean isExpired(Project project, Disposable owner, AsyncTaskHandle handle) {
        return handle.isCancellationRequested() || project.isDisposed() || Disposer.isDisposed(owner);
    }

    private static void release(LifecycleRegistration registration) {
        if (registration != null) {
            registration.release();
            if (!Disposer.isDisposed(registration)) {
                Disposer.dispose(registration);
            }
        }
    }

    private static final class Completion implements Runnable {
        private final Operation operation;
        private final AsyncTaskHandle handle;
        private final LifecycleRegistration ownerRegistration;
        private final LifecycleRegistration projectRegistration;
        private final AtomicBoolean completed = new AtomicBoolean();

        private Completion(Operation operation, AsyncTaskHandle handle,
                           LifecycleRegistration ownerRegistration,
                           LifecycleRegistration projectRegistration) {
            this.operation = operation;
            this.handle = handle;
            this.ownerRegistration = ownerRegistration;
            this.projectRegistration = projectRegistration;
        }

        @Override
        public void run() {
            if (!completed.compareAndSet(false, true)) {
                return;
            }
            if (operation != null) {
                OPERATIONS.remove(operation, handle);
            }
            release(ownerRegistration);
            release(projectRegistration);
            handle.complete();
        }
    }

    @FunctionalInterface
    interface UiScheduler {
        void schedule(Runnable runnable, BooleanSupplier expired);
    }

    private static final class LifecycleRegistration implements Disposable {
        private final AsyncTaskHandle handle;
        private volatile boolean released;

        private LifecycleRegistration(AsyncTaskHandle handle) {
            this.handle = handle;
        }

        private void release() {
            released = true;
        }

        @Override
        public void dispose() {
            if (!released) {
                handle.cancel();
            }
        }
    }

    private static final class Operation {
        private final Disposable owner;
        private final Object key;
        private final int hashCode;

        private Operation(Disposable owner, Object key) {
            this.owner = owner;
            this.key = key;
            this.hashCode = 31 * System.identityHashCode(owner) + key.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Operation)) {
                return false;
            }
            Operation operation = (Operation) other;
            return owner == operation.owner && key.equals(operation.key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
