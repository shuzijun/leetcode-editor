package com.shuzijun.leetcode.plugin.manager;

import com.shuzijun.leetcode.plugin.model.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FindManagerTest {

    @Test
    public void preservesLocalDifficultyAndStatusContracts() {
        List<Tag> difficulties = FindManager.getDifficulty();
        List<Tag> statuses = FindManager.getStatus();

        assertEquals(3, difficulties.size());
        assertEquals("Easy", difficulties.get(0).getName());
        assertEquals("EASY", difficulties.get(0).getSlug());
        assertEquals("Medium", difficulties.get(1).getName());
        assertEquals("MEDIUM", difficulties.get(1).getSlug());
        assertEquals("Hard", difficulties.get(2).getName());
        assertEquals("HARD", difficulties.get(2).getSlug());

        assertEquals(3, statuses.size());
        assertEquals("Todo", statuses.get(0).getName());
        assertEquals("NOT_STARTED", statuses.get(0).getSlug());
        assertEquals("Solved", statuses.get(1).getName());
        assertEquals("AC", statuses.get(1).getSlug());
        assertEquals("Attempted", statuses.get(2).getName());
        assertEquals("TRIED", statuses.get(2).getSlug());
    }
}
