package com.shuzijun.leetcode.plugin.application;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Prevents an obsolete login callback from publishing state after a newer
 * login, logout, or site change.
 */
public final class LoginGenerationTracker {

    private static final AtomicLong GENERATION = new AtomicLong();

    private LoginGenerationTracker() {
    }

    public static long next() {
        return GENERATION.incrementAndGet();
    }

    public static boolean isCurrent(long generation) {
        return GENERATION.get() == generation;
    }
}
