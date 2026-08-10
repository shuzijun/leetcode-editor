package com.shuzijun.leetcode.plugin.application;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Striped;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

final class ShortLivedCache<T> {

    private final Cache<String, T> cache;
    private final Striped<Lock> locks = Striped.lazyWeakLock(16);

    ShortLivedCache(long duration, TimeUnit unit) {
        this(CacheBuilder.newBuilder()
                .expireAfterWrite(duration, unit)
                .build());
    }

    ShortLivedCache(long maximumSize) {
        this(CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .build());
    }

    ShortLivedCache(long maximumSize, long duration, TimeUnit unit) {
        this(CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(duration, unit)
                .build());
    }

    private ShortLivedCache(Cache<Object, Object> cache) {
        @SuppressWarnings("unchecked")
        Cache<String, T> typedCache = (Cache<String, T>) (Cache<?, ?>) cache;
        this.cache = typedCache;
    }

    T get(String key, Loader<T> loader) throws Exception {
        T value = cache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        Lock lock = locks.get(key);
        lock.lock();
        try {
            value = cache.getIfPresent(key);
            if (value == null) {
                value = loader.load();
                if (value != null) {
                    cache.put(key, value);
                }
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    T getIfPresent(String key) {
        return cache.getIfPresent(key);
    }

    void put(String key, T value) {
        cache.put(key, value);
    }

    void invalidate(String key) {
        cache.invalidate(key);
    }

    void invalidateAll() {
        cache.invalidateAll();
    }

    void invalidateMatching(Predicate<String> predicate) {
        cache.asMap().keySet().removeIf(predicate);
    }

    @FunctionalInterface
    interface Loader<T> {
        T load() throws Exception;
    }
}
