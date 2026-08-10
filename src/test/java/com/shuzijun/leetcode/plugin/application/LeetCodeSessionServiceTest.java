package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LeetCodeSessionServiceTest {

    @Test
    public void separatesCacheByHostAndUser() {
        assertEquals("leetcode.com\nuser-a",
                LeetCodeSessionService.cacheKey("leetcode.com", "user-a"));
        assertEquals("leetcode.cn\nuser-a",
                LeetCodeSessionService.cacheKey("leetcode.cn", "user-a"));
        assertEquals("leetcode.com\n",
                LeetCodeSessionService.cacheKey("leetcode.com", null));
    }

    @Test
    public void switchInvalidatesSessionAndQuestionPageCaches() throws Exception {
        ShortLivedCache<Object> sessionCache = sessionCache();
        ShortLivedCache<Object> pageCache = apiCache("QUESTION_PAGE_CACHE");
        sessionCache.put("leetcode.com\nuser-a", Collections.emptyList());
        pageCache.put("leetcode.com\nuser-a\n1\n50\nall\nfilters", new Object());

        LeetCodeSessionService.invalidateAfterSwitch();

        assertNull(sessionCache.getIfPresent("leetcode.com\nuser-a"));
        assertNull(pageCache.getIfPresent(
                "leetcode.com\nuser-a\n1\n50\nall\nfilters"
        ));
    }

    @SuppressWarnings("unchecked")
    private static ShortLivedCache<Object> apiCache(String fieldName) throws Exception {
        Field field = LeetCodeApiService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (ShortLivedCache<Object>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private static ShortLivedCache<Object> sessionCache() throws Exception {
        Field field = LeetCodeSessionService.class.getDeclaredField("SESSION_CACHE");
        field.setAccessible(true);
        return (ShortLivedCache<Object>) field.get(null);
    }
}
