package com.shuzijun.leetcode.plugin.manager;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NavigatorRequestTrackerTest {

    @Test
    public void onlyAcceptsTheLatestRequestForAnAction() {
        NavigatorAction.Adapter<?> navigatorAction = new NavigatorAction.Adapter<>();

        long firstRequest = NavigatorRequestTracker.begin(navigatorAction);
        long secondRequest = NavigatorRequestTracker.begin(navigatorAction);

        assertFalse(NavigatorRequestTracker.isLatest(navigatorAction, firstRequest));
        assertTrue(NavigatorRequestTracker.isLatest(navigatorAction, secondRequest));
    }
}
