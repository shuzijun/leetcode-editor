package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginGenerationTrackerTest {

    @Test
    public void onlyTheLatestLoginGenerationRemainsCurrent() {
        long first = LoginGenerationTracker.next();
        assertTrue(LoginGenerationTracker.isCurrent(first));

        long second = LoginGenerationTracker.next();

        assertFalse(LoginGenerationTracker.isCurrent(first));
        assertTrue(LoginGenerationTracker.isCurrent(second));
    }
}
