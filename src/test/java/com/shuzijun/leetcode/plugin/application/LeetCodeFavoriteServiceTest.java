package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.model.FavoriteResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LeetCodeFavoriteServiceTest {

    @Test
    public void mapsTypedSuccessAndServerError() {
        FavoriteResult sdkSuccess = new FavoriteResult();
        sdkSuccess.setOk(true);
        FavoriteResult sdkFailure = new FavoriteResult();
        sdkFailure.setOk(false);
        sdkFailure.setError("denied");

        assertTrue(sdkSuccess.isOk());
        assertNull(sdkSuccess.getError());
        assertFalse(sdkFailure.isOk());
        assertEquals("denied", sdkFailure.getError());
    }
}
