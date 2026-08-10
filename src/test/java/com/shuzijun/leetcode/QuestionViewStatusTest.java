package com.shuzijun.leetcode;

import com.shuzijun.lc.model.QuestionView;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuestionViewStatusTest {

    @Test
    public void acceptedStatusUsesUtf8CheckMark() {
        QuestionView question = new QuestionView();
        question.setStatus("ac");

        assertEquals("\u2714", question.getStatusSign());
    }

    @Test
    public void normalizesMissingLevelAndPercentageFrequencyOnWrite() {
        QuestionView question = new QuestionView();

        assertEquals(Integer.valueOf(0), question.getLevel());
        question.setFrequency(25.0d);
        assertEquals(0.25d, question.getFrequency(), 0.0001d);
        assertEquals(0.25d, question.getFrequency(), 0.0001d);
    }
}
