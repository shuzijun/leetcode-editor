package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.Disposable;
import com.shuzijun.lc.CancellationToken;
import com.shuzijun.lc.RequestContext;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Cancellation handle shared by an asynchronous UI task and its SDK requests. */
public final class AsyncTaskHandle implements Disposable, CancellationToken {
    private static final int ACTIVE = 0;
    private static final int CANCELLED = 1;
    private static final int COMPLETED = 2;

    private final AtomicInteger state = new AtomicInteger(ACTIVE);
    private final AtomicReference<Future<?>> future = new AtomicReference<>();
    private final AtomicReference<Runnable> cancellationAction = new AtomicReference<>();
    private final RequestContext requestContext = RequestContext.builder()
            .cancellationToken(this)
            .build();

    public boolean cancel() {
        if (!state.compareAndSet(ACTIVE, CANCELLED)) {
            return false;
        }
        Future<?> current = future.get();
        if (current != null) {
            current.cancel(true);
        }
        Runnable action = cancellationAction.getAndSet(null);
        if (action != null) {
            action.run();
        }
        return true;
    }

    @Override
    public void dispose() {
        cancel();
    }

    @Override
    public boolean isCancellationRequested() {
        return state.get() == CANCELLED;
    }

    public boolean isDone() {
        return state.get() != ACTIVE;
    }

    public CancellationToken getCancellationToken() {
        return this;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    void attach(Future<?> submittedFuture) {
        if (!future.compareAndSet(null, submittedFuture)) {
            submittedFuture.cancel(true);
            throw new IllegalStateException("Async task already submitted");
        }
        if (isDone()) {
            submittedFuture.cancel(true);
        }
    }

    void complete() {
        state.compareAndSet(ACTIVE, COMPLETED);
        future.set(null);
        cancellationAction.set(null);
    }

    void onCancellation(Runnable action) {
        if (!cancellationAction.compareAndSet(null, action)) {
            throw new IllegalStateException("Cancellation action already registered");
        }
        if (isCancellationRequested()) {
            Runnable registered = cancellationAction.getAndSet(null);
            if (registered != null) {
                registered.run();
            }
        }
    }
}
