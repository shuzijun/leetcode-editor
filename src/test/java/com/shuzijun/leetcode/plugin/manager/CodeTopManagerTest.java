package com.shuzijun.leetcode.plugin.manager;

import com.shuzijun.lc.http.HttpResponse;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeTopManagerTest {

    @Test
    public void treatsNullAndNon200ResponsesAsFailures() {
        assertFalse(CodeTopManager.isSuccessful(null));

        HttpResponse failure = new HttpResponse(500);
        assertFalse(CodeTopManager.isSuccessful(failure));

        HttpResponse success = new HttpResponse(200);
        assertTrue(CodeTopManager.isSuccessful(success));
    }
}
