package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.lc.model.Question;
import com.shuzijun.lc.model.Tag;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultQuestionPresentationStrategyTest {

    @Test
    public void rendersExpandedTopicsAndVotes() {
        Question question = new Question();
        question.setContent("<p>content</p>");
        question.setTranslatedContent("<p>内容</p>");
        question.setLikes(100);
        question.setDislikes(5);
        Tag topic = new Tag();
        topic.setName("Array");
        topic.setTranslatedName("数组");
        question.setTopicTags(Collections.singletonList(topic));

        String content = new DefaultQuestionPresentationStrategy()
                .renderContent(question, true, true);

        assertTrue(content.contains("<p>内容</p>"));
        assertTrue(content.contains("<div><div>Related Topics</div><div>"));
        assertTrue(content.contains("<li>数组</li>"));
        assertTrue(content.contains("<li>👍 100</li><li>👎 5</li>"));
    }

    @Test
    public void omitsTopicsWhenDisabled() {
        Question question = new Question();
        question.setContent("<p>content</p>");
        question.setLikes(1);
        question.setDislikes(0);

        String content = new DefaultQuestionPresentationStrategy()
                .renderContent(question, false, false);

        assertFalse(content.contains("Related Topics"));
        assertTrue(content.contains("<p>content</p>"));
    }
}
