package com.shuzijun.leetcode.plugin.application;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SingleFlightRequestRegistryTest {

    private ExecutorService executor;
    private SingleFlightRequestRegistry<SingleFlightRequestRegistry.RequestKey, MutableResult> registry;

    @Before
    public void setUp() {
        executor = Executors.newCachedThreadPool();
        registry = new SingleFlightRequestRegistry<>(executor);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    public void coalescesSameKeyAndReturnsIndependentCopies() throws Exception {
        CountDownLatch loaderStarted = new CountDownLatch(1);
        CountDownLatch releaseLoader = new CountDownLatch(1);
        AtomicInteger loads = new AtomicInteger();
        SingleFlightRequestRegistry.RequestKey key = key("leetcode.com", "two-sum", "java", "light");

        SingleFlightRequestRegistry.Subscription<MutableResult> first = registry.subscribe(key, () -> {
            loads.incrementAndGet();
            loaderStarted.countDown();
            assertTrue(releaseLoader.await(5, TimeUnit.SECONDS));
            return new MutableResult("detail");
        }, MutableResult::copy);
        assertTrue(loaderStarted.await(5, TimeUnit.SECONDS));
        SingleFlightRequestRegistry.Subscription<MutableResult> second =
                registry.subscribe(key, () -> {
                    fail("same key must reuse the active request");
                    return null;
                }, MutableResult::copy);

        releaseLoader.countDown();
        MutableResult firstResult = first.future().get(5, TimeUnit.SECONDS);
        MutableResult secondResult = second.future().get(5, TimeUnit.SECONDS);

        assertEquals(1, loads.get());
        assertNotSame(firstResult, secondResult);
        firstResult.values.add("consumer-one");
        assertEquals(1, secondResult.values.size());
    }

    @Test
    public void isolatesEveryRequestDimensionAndResource() throws Exception {
        List<SingleFlightRequestRegistry.RequestKey> keys = List.of(
                key("leetcode.com", "two-sum", "java", "light"),
                key("leetcode.cn", "two-sum", "java", "light"),
                key("leetcode.com", "three-sum", "java", "light"),
                key("leetcode.com", "two-sum", "kotlin", "light"),
                key("leetcode.com", "two-sum", "java", "dark"),
                new SingleFlightRequestRegistry.RequestKey(
                        "leetcode.com", "two-sum", "java", "light", "submission:1")
        );
        AtomicInteger loads = new AtomicInteger();
        List<SingleFlightRequestRegistry.Subscription<MutableResult>> subscriptions = new ArrayList<>();

        for (SingleFlightRequestRegistry.RequestKey key : keys) {
            subscriptions.add(registry.subscribe(
                    key,
                    () -> new MutableResult(Integer.toString(loads.incrementAndGet())),
                    MutableResult::copy
            ));
        }
        for (SingleFlightRequestRegistry.Subscription<MutableResult> subscription : subscriptions) {
            subscription.future().get(5, TimeUnit.SECONDS);
        }

        assertEquals(keys.size(), loads.get());
    }

    @Test
    public void cancellingOneConsumerKeepsSharedRequestRunning() throws Exception {
        CountDownLatch loaderStarted = new CountDownLatch(1);
        CountDownLatch releaseLoader = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        SingleFlightRequestRegistry.RequestKey key = key("leetcode.com", "two-sum", "java", "light");

        SingleFlightRequestRegistry.Subscription<MutableResult> first = registry.subscribe(key, () -> {
            loaderStarted.countDown();
            try {
                releaseLoader.await();
            } catch (InterruptedException error) {
                interrupted.set(true);
                throw error;
            }
            return new MutableResult("detail");
        }, MutableResult::copy);
        assertTrue(loaderStarted.await(5, TimeUnit.SECONDS));
        SingleFlightRequestRegistry.Subscription<MutableResult> second =
                registry.subscribe(key, () -> new MutableResult("unused"), MutableResult::copy);

        assertTrue(first.cancel());
        assertTrue(first.isCancelled());
        assertFalse(interrupted.get());
        releaseLoader.countDown();

        assertEquals("detail", second.future().get(5, TimeUnit.SECONDS).values.get(0));
        assertFalse(interrupted.get());
    }

    @Test
    public void cancellingLastConsumerCancelsUnderlyingTask() throws Exception {
        CountDownLatch loaderStarted = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        SingleFlightRequestRegistry.RequestKey key = key("leetcode.com", "two-sum", "java", "light");

        SingleFlightRequestRegistry.Subscription<MutableResult> subscription = registry.subscribe(key, () -> {
            loaderStarted.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException error) {
                interrupted.countDown();
                throw error;
            }
            return new MutableResult("unreachable");
        }, MutableResult::copy);

        assertTrue(loaderStarted.await(5, TimeUnit.SECONDS));
        assertTrue(subscription.cancel());
        assertTrue(interrupted.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void failureIsNotCachedAndNextSubscriptionRetries() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        SingleFlightRequestRegistry.RequestKey key = key("leetcode.com", "two-sum", "java", "light");

        SingleFlightRequestRegistry.Subscription<MutableResult> failed = registry.subscribe(key, () -> {
            loads.incrementAndGet();
            throw new IllegalStateException("network");
        }, MutableResult::copy);
        try {
            failed.future().get(5, TimeUnit.SECONDS);
            fail("failure should reach the consumer");
        } catch (ExecutionException expected) {
            assertEquals("network", expected.getCause().getMessage());
        }

        MutableResult retried = registry.subscribe(key, () -> {
            loads.incrementAndGet();
            return new MutableResult("retried");
        }, MutableResult::copy).future().get(5, TimeUnit.SECONDS);

        assertEquals(2, loads.get());
        assertEquals("retried", retried.values.get(0));
    }

    private static SingleFlightRequestRegistry.RequestKey key(
            String host,
            String titleSlug,
            String language,
            String renderSettings
    ) {
        return new SingleFlightRequestRegistry.RequestKey(host, titleSlug, language, renderSettings);
    }

    private static final class MutableResult {
        private final List<String> values = new ArrayList<>();

        private MutableResult(String value) {
            values.add(value);
        }

        private MutableResult copy() {
            MutableResult copy = new MutableResult(values.get(0));
            copy.values.clear();
            copy.values.addAll(values);
            return copy;
        }
    }
}
