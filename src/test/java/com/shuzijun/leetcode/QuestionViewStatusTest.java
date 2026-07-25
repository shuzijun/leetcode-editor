package com.shuzijun.leetcode;

import com.shuzijun.leetcode.plugin.model.QuestionView;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuestionViewStatusTest {

    @Test
    public void acceptedStatusUsesUtf8CheckMark() {
        QuestionView question = new QuestionView();
        question.setStatus("ac");

        assertEquals("\u2714", question.getStatusSign());
    }
}
