package com.shuzijun.leetcode.plugin.editor.converge;

import com.shuzijun.leetcode.plugin.editor.SplitFileEditor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolutionPreviewTest {

    @Test
    public void showsSolutionDetailAfterLoading() {
        assertEquals(
                SplitFileEditor.SplitEditorLayout.SECOND,
                SolutionPreview.solutionDetailLayout()
        );
    }
}
