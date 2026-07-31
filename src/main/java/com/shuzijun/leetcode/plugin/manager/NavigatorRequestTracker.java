package com.shuzijun.leetcode.plugin.manager;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

final class NavigatorRequestTracker {

    private static final Map<NavigatorAction<?>, AtomicLong> REQUEST_VERSIONS = new WeakHashMap<>();

    private NavigatorRequestTracker() {
    }

    static long begin(NavigatorAction<?> navigatorAction) {
        synchronized (REQUEST_VERSIONS) {
            return REQUEST_VERSIONS.computeIfAbsent(navigatorAction, ignored -> new AtomicLong()).incrementAndGet();
        }
    }

    static boolean isLatest(NavigatorAction<?> navigatorAction, long version) {
        synchronized (REQUEST_VERSIONS) {
            AtomicLong latestVersion = REQUEST_VERSIONS.get(navigatorAction);
            return latestVersion != null && latestVersion.get() == version;
        }
    }
}
