package com.shuzijun.leetcode.plugin.editor.converge;

import com.shuzijun.leetcode.plugin.editor.LCVPreview;
import com.shuzijun.leetcode.plugin.editor.SplitFileEditor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubmissionsPreviewTest {

    @Test
    public void createsHtmlPreviewForSubmissionDetails() {
        assertEquals(LCVPreview.class, SubmissionsPreview.submissionPreviewType());
    }

    @Test
    public void showsSubmissionDetailAfterLoading() {
        assertEquals(
                SplitFileEditor.SplitEditorLayout.SECOND,
                SubmissionsPreview.submissionDetailLayout()
        );
    }
}
