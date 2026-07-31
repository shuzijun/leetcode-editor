package com.shuzijun.leetcode.plugin.manager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoteManagerTest {

    @Test
    public void extractsTheRemoteNoteContentFromTheGraphqlResponse() {
        String note = NoteManager.extractNote("{\"data\":{\"question\":{\"note\":\"# Two Sum\\nUse a map.\"}}}");

        assertEquals("# Two Sum\nUse a map.", note);
    }
}
