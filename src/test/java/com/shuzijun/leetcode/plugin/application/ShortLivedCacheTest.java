package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ShortLivedCacheTest {

    @Test
    public void cachesValuesAndInvalidatesOnlyMatchingKeys() throws Exception {
        ShortLivedCache<Integer> cache = new ShortLivedCache<>(30, TimeUnit.SECONDS);
        AtomicInteger loads = new AtomicInteger();

        assertEquals(Integer.valueOf(1), cache.get("en\npage-1", loads::incrementAndGet));
        assertEquals(Integer.valueOf(1), cache.get("en\npage-1", loads::incrementAndGet));
        assertEquals(Integer.valueOf(2), cache.get("cn\npage-1", loads::incrementAndGet));

        cache.invalidateMatching(key -> key.startsWith("en\n"));

        assertEquals(Integer.valueOf(3), cache.get("en\npage-1", loads::incrementAndGet));
        assertEquals(Integer.valueOf(2), cache.get("cn\npage-1", loads::incrementAndGet));
        assertEquals(3, loads.get());
    }

    @Test
    public void doesNotCacheNullValues() throws Exception {
        ShortLivedCache<Integer> cache = new ShortLivedCache<>(5);
        AtomicInteger loads = new AtomicInteger();

        assertNull(cache.get("daily", () -> {
            loads.incrementAndGet();
            return null;
        }));
        assertNull(cache.get("daily", () -> {
            loads.incrementAndGet();
            return null;
        }));

        assertEquals(2, loads.get());
    }
}
