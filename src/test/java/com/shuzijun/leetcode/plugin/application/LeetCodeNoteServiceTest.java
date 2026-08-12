package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.model.NoteUpdateResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LeetCodeNoteServiceTest {

    @Test
    public void mapsTypedUpdateResultWithoutDroppingServerErrorOrNote() {
        NoteUpdateResult sdkResult = new NoteUpdateResult();
        sdkResult.setSuccess(false);
        sdkResult.setError("note rejected");
        sdkResult.setNote("server note");

        assertFalse(sdkResult.isSuccess());
        assertEquals("note rejected", sdkResult.getError());
        assertEquals("server note", sdkResult.getNote());
    }
}
