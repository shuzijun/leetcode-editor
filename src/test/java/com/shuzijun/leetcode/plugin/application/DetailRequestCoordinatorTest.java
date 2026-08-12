package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.errors.LcException;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DetailRequestCoordinatorTest {

    @Test
    public void permitsNullSdkResponse() throws Exception {
        String result = DetailRequestCoordinator.load(
                key("null"),
                context -> null,
                value -> value
        );

        assertNull(result);
    }

    @Test
    public void preservesSdkException() throws Exception {
        LcException failure = new LcException("network");

        try {
            DetailRequestCoordinator.load(
                    key("failure"),
                    context -> {
                        throw failure;
                    },
                    value -> value
            );
            fail("Expected request failure");
        } catch (LcException actual) {
            assertSame(failure, actual);
        }
    }

    @Test
    public void interruptionCancelsLastSubscriptionAndSdkContext() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        AtomicBoolean tokenCancelled = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread consumer = new Thread(() -> {
            try {
                DetailRequestCoordinator.load(
                        key("cancel"),
                        context -> {
                            started.countDown();
                            while (!context.getCancellationToken().isCancellationRequested()) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException exception) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            tokenCancelled.set(true);
                            cancelled.countDown();
                            throw new LcException("cancelled");
                        },
                        value -> value
                );
            } catch (LcException exception) {
                failure.set(exception);
            }
        });
        consumer.start();

        assertTrue(started.await(5, TimeUnit.SECONDS));
        consumer.interrupt();
        consumer.join(5000);

        assertFalse(consumer.isAlive());
        assertTrue(cancelled.await(5, TimeUnit.SECONDS));
        assertTrue(tokenCancelled.get());
        assertEquals("Detail request interrupted", failure.get().getMessage());
    }

    private static SingleFlightRequestRegistry.RequestKey key(String resource) {
        return new SingleFlightRequestRegistry.RequestKey(
                "leetcode.com",
                "two-sum",
                "java",
                "markdown",
                resource
        );
    }
}
