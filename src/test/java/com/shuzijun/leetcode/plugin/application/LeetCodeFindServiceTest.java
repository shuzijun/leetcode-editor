package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.leetcode.plugin.model.Tag;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeetCodeFindServiceTest {

    @Test
    public void mapsTranslatedTopicsAndCopiesQuestionIds() {
        com.shuzijun.lc.model.Tag sdkTag = sdkTag();

        Tag tag = LcModelMapper.toTag(sdkTag, true);

        assertEquals("array", tag.getSlug());
        assertEquals("数组", tag.getName());
        assertEquals("topic", tag.getType());
        assertTrue(tag.getQuestions().contains("1"));
        assertTrue(tag.getQuestions().contains("2"));
    }

    @Test
    public void fallsBackToEnglishNameAndKeepsUiSelectionIsolated() {
        com.shuzijun.lc.model.Tag sdkTag = sdkTag();
        sdkTag.setTranslatedName("");

        Tag first = LcModelMapper.toTag(sdkTag, true);
        Tag second = LcModelMapper.toTag(sdkTag, true);
        first.setSelect(true);

        assertEquals("Array", first.getName());
        assertTrue(first.isSelect());
        assertFalse(second.isSelect());
    }

    @Test
    public void restoresLegacyCategoryUrlAndSeparatesCacheKeys() {
        com.shuzijun.lc.model.Tag sdkTag = new com.shuzijun.lc.model.Tag();
        sdkTag.setSlug("algorithms");
        sdkTag.setName("Algorithms");
        sdkTag.setType("/problemset/algorithms/");

        Tag category = LeetCodeFindService.toCategory(
                sdkTag,
                "https://leetcode.cn/"
        );

        assertEquals(
                "https://leetcode.cn/api/problems/algorithms/",
                category.getType()
        );
        assertEquals(
                "leetcode.cn\nlists\nuser-a",
                LeetCodeFindService.cacheKey("leetcode.cn", "lists", "user-a")
        );
        assertEquals(
                "leetcode.cn\nlists\nuser-b",
                LeetCodeFindService.cacheKey("leetcode.cn", "lists", "user-b")
        );
        assertEquals(
                "leetcode.com\ntags\n",
                LeetCodeFindService.cacheKey("leetcode.com", "tags", null)
        );
    }

    private static com.shuzijun.lc.model.Tag sdkTag() {
        com.shuzijun.lc.model.Tag tag = new com.shuzijun.lc.model.Tag();
        tag.setSlug("array");
        tag.setName("Array");
        tag.setTranslatedName("数组");
        tag.setType("topic");
        tag.setQuestions(new HashSet<>(Arrays.asList("1", "2")));
        return tag;
    }
}
