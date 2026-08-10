package com.shuzijun.leetcode.plugin.window;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NavigatorTabsPanelTest {

    @Test
    public void supportsConcurrentIterationAndRemoval() throws Exception {
        NavigatorTabsPanel.DisposableMap<Integer, Integer> map =
                new NavigatorTabsPanel.DisposableMap<>();
        for (int index = 0; index < 10_000; index++) {
            map.put(index, index);
        }

        CountDownLatch iterationStarted = new CountDownLatch(1);
        AtomicReference<Throwable> iterationFailure = new AtomicReference<>();
        Thread iterator = new Thread(() -> {
            try {
                iterationStarted.countDown();
                for (Integer ignored : map.values()) {
                    Thread.yield();
                }
            } catch (Throwable throwable) {
                iterationFailure.set(throwable);
            }
        });

        iterator.start();
        assertTrue(iterationStarted.await(5, TimeUnit.SECONDS));
        for (int index = 0; index < 10_000; index++) {
            map.remove(index);
        }
        iterator.join(TimeUnit.SECONDS.toMillis(5));

        assertFalse(iterator.isAlive());
        assertNull(iterationFailure.get());
    }

    @Test
    public void returnsAnExistingDifferentKey() {
        NavigatorTabsPanel.DisposableMap<String, Integer> map =
                new NavigatorTabsPanel.DisposableMap<>();
        map.put("current", 1);
        map.put("other", 2);

        assertEquals("other", map.getOtherKey("current"));
    }
}
