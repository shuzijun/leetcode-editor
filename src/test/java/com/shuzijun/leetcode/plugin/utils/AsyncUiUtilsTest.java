package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AsyncUiUtilsTest {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @After
    public void tearDown() throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    public void exposesCancellationToSdkContext() {
        AsyncTaskHandle handle = new AsyncTaskHandle();

        assertSame(handle, handle.getCancellationToken());
        assertSame(handle, handle.getRequestContext().getCancellationToken());
        assertFalse(handle.isCancellationRequested());

        assertTrue(handle.cancel());
        assertFalse(handle.cancel());
        assertTrue(handle.isCancellationRequested());
    }

    @Test
    public void keyedLoadCancelsPreviousOperationForSameOwner() throws Exception {
        Disposable owner = Disposer.newDisposable();
        Project project = project();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        QueueScheduler scheduler = new QueueScheduler();
        try {
            AsyncTaskHandle first = AsyncUiUtils.load(project, owner, "preview", () -> {
                firstStarted.countDown();
                releaseFirst.await();
                return "first";
            }, (result, error) -> {
            }, executor, scheduler);
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

            AsyncTaskHandle second = AsyncUiUtils.load(project, owner, "preview",
                    () -> "second", (result, error) -> {
                    }, executor, scheduler);

            assertTrue(first.isCancellationRequested());
            assertFalse(second.isCancellationRequested());
        } finally {
            releaseFirst.countDown();
            Disposer.dispose(owner);
        }
    }

    @Test
    public void ownerDisposalCancelsTaskAndSuppressesQueuedUiCallback() throws Exception {
        Disposable owner = Disposer.newDisposable();
        Project project = project();
        CountDownLatch backgroundDone = new CountDownLatch(1);
        QueueScheduler scheduler = new QueueScheduler();
        AtomicBoolean callbackInvoked = new AtomicBoolean();

        AsyncTaskHandle handle = AsyncUiUtils.load(project, owner, null, () -> {
            backgroundDone.countDown();
            return "loaded";
        }, (result, error) -> callbackInvoked.set(true), executor, scheduler);
        assertTrue(backgroundDone.await(5, TimeUnit.SECONDS));
        assertTrue(scheduler.awaitScheduled());

        Disposer.dispose(owner);
        scheduler.runAll();

        assertTrue(handle.isCancellationRequested());
        assertTrue(handle.isDone());
        assertFalse(callbackInvoked.get());
    }

    @Test
    public void explicitCancellationSuppressesQueuedUiCallback() throws Exception {
        Disposable owner = Disposer.newDisposable();
        Project project = project();
        CountDownLatch backgroundDone = new CountDownLatch(1);
        QueueScheduler scheduler = new QueueScheduler();
        AtomicBoolean callbackInvoked = new AtomicBoolean();
        try {
            AsyncTaskHandle handle = AsyncUiUtils.load(project, owner, null, () -> {
                backgroundDone.countDown();
                return "loaded";
            }, (result, error) -> callbackInvoked.set(true), executor, scheduler);
            assertTrue(backgroundDone.await(5, TimeUnit.SECONDS));
            assertTrue(scheduler.awaitScheduled());

            handle.cancel();
            scheduler.runAll();

            assertTrue(handle.isDone());
            assertFalse(callbackInvoked.get());
        } finally {
            Disposer.dispose(owner);
        }
    }

    @Test
    public void completedTaskCannotTransitionBackToCancelled() throws Exception {
        Disposable owner = Disposer.newDisposable();
        Project project = project();
        QueueScheduler scheduler = new QueueScheduler();
        AtomicBoolean callbackInvoked = new AtomicBoolean();
        try {
            AsyncTaskHandle handle = AsyncUiUtils.load(project, owner, null,
                    () -> "loaded", (result, error) -> callbackInvoked.set(true), executor, scheduler);
            assertTrue(scheduler.awaitScheduled());

            scheduler.runAll();

            assertTrue(handle.isDone());
            assertFalse(handle.isCancellationRequested());
            assertFalse(handle.cancel());
            assertFalse(handle.isCancellationRequested());
            assertTrue(callbackInvoked.get());
        } finally {
            Disposer.dispose(owner);
        }
    }

    private static Project project() {
        AtomicBoolean disposed = new AtomicBoolean();
        return (Project) Proxy.newProxyInstance(
                Project.class.getClassLoader(),
                new Class<?>[]{Project.class},
                (proxy, method, args) -> {
                    if ("isDisposed".equals(method.getName())) {
                        return disposed.get();
                    }
                    if ("dispose".equals(method.getName())) {
                        disposed.set(true);
                        return null;
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("toString".equals(method.getName())) {
                        return "TestProject";
                    }
                    Class<?> returnType = method.getReturnType();
                    if (!returnType.isPrimitive()) {
                        return null;
                    }
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == char.class) {
                        return '\0';
                    }
                    return 0;
                });
    }

    private static final class QueueScheduler implements AsyncUiUtils.UiScheduler {
        private final Queue<Runnable> queue = new ArrayDeque<>();
        private final CountDownLatch scheduled = new CountDownLatch(1);

        @Override
        public synchronized void schedule(Runnable runnable, java.util.function.BooleanSupplier expired) {
            queue.add(runnable);
            scheduled.countDown();
        }

        private boolean awaitScheduled() throws InterruptedException {
            return scheduled.await(5, TimeUnit.SECONDS);
        }

        private void runAll() {
            while (true) {
                Runnable runnable;
                synchronized (this) {
                    runnable = queue.poll();
                }
                if (runnable == null) {
                    return;
                }
                runnable.run();
            }
        }
    }
}
