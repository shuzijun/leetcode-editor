package com.shuzijun.leetcode.plugin.application;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.shuzijun.lc.CancellationToken;
import com.shuzijun.lc.RequestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service(Service.Level.PROJECT)
public final class CodeExecutionCoordinator implements Disposable {

    private final ConcurrentMap<ExecutionKey, Execution> activeExecutions = new ConcurrentHashMap<>();
    private final AtomicBoolean disposed = new AtomicBoolean();

    @NotNull
    public static CodeExecutionCoordinator getInstance(@NotNull Project project) {
        return project.getService(CodeExecutionCoordinator.class);
    }

    @Nullable
    public Execution tryStart(@NotNull String titleSlug, @NotNull ExecutionType type) {
        if (disposed.get()) {
            return null;
        }
        ExecutionKey key = new ExecutionKey(titleSlug, type);
        Execution execution = new Execution(this, key);
        if (activeExecutions.putIfAbsent(key, execution) != null) {
            return null;
        }
        if (disposed.get()) {
            execution.cancel();
            return null;
        }
        return execution;
    }

    public boolean isActive(@NotNull String titleSlug, @NotNull ExecutionType type) {
        return activeExecutions.containsKey(new ExecutionKey(titleSlug, type));
    }

    public boolean cancel(@NotNull String titleSlug, @NotNull ExecutionType type) {
        Execution execution = activeExecutions.get(new ExecutionKey(titleSlug, type));
        return execution != null && execution.cancel();
    }

    @Override
    public void dispose() {
        if (!disposed.compareAndSet(false, true)) {
            return;
        }
        for (Execution execution : activeExecutions.values()) {
            execution.cancel();
        }
        activeExecutions.clear();
    }

    private void finish(Execution execution, CodeExecutionState terminalState) {
        execution.state.set(terminalState);
        activeExecutions.remove(execution.key, execution);
    }

    public enum ExecutionType {
        RUN,
        SUBMIT
    }

    public static final class Execution implements CancellationToken {
        private final CodeExecutionCoordinator coordinator;
        private final ExecutionKey key;
        private final AtomicBoolean cancellationRequested = new AtomicBoolean();
        private final AtomicBoolean finished = new AtomicBoolean();
        private final AtomicReference<CodeExecutionState> state =
                new AtomicReference<>(CodeExecutionState.STARTING);
        private final RequestContext requestContext = RequestContext.builder()
                .cancellationToken(this)
                .build();

        private Execution(CodeExecutionCoordinator coordinator, ExecutionKey key) {
            this.coordinator = coordinator;
            this.key = key;
        }

        @NotNull
        public RequestContext getRequestContext() {
            return requestContext;
        }

        @NotNull
        public CodeExecutionState getState() {
            return state.get();
        }

        public void polling() {
            state.compareAndSet(CodeExecutionState.STARTING, CodeExecutionState.POLLING);
        }

        public void succeeded() {
            finish(CodeExecutionState.SUCCEEDED);
        }

        public void failed() {
            finish(CodeExecutionState.FAILED);
        }

        public void timedOut() {
            finish(CodeExecutionState.TIMED_OUT);
        }

        public boolean cancel() {
            cancellationRequested.set(true);
            return finish(CodeExecutionState.CANCELLED);
        }

        @Override
        public boolean isCancellationRequested() {
            return cancellationRequested.get();
        }

        private boolean finish(CodeExecutionState terminalState) {
            if (!finished.compareAndSet(false, true)) {
                return false;
            }
            coordinator.finish(this, terminalState);
            return true;
        }
    }

    private static final class ExecutionKey {
        private final String titleSlug;
        private final ExecutionType type;

        private ExecutionKey(String titleSlug, ExecutionType type) {
            this.titleSlug = titleSlug;
            this.type = type;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ExecutionKey)) {
                return false;
            }
            ExecutionKey that = (ExecutionKey) other;
            return titleSlug.equals(that.titleSlug) && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(titleSlug, type);
        }
    }
}
