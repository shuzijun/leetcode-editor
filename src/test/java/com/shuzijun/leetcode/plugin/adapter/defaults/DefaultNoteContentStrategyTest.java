package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultNoteContentStrategyTest {

    @Test
    public void initializesMissingDefaultNoteAsEmptyContent() {
        assertEquals(
                "",
                new DefaultNoteContentStrategy()
                        .initialContent(new Question(), CodeTypeEnum.JAVA)
        );
    }
}
