package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.lc.model.Question;
import com.shuzijun.lc.model.Tag;
import com.shuzijun.leetcode.plugin.spi.QuestionPresentationStrategy;
import com.shuzijun.leetcode.plugin.utils.doc.CleanMarkdown;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DefaultQuestionPresentationStrategy implements QuestionPresentationStrategy {

    @Override
    public @NotNull String renderContent(
            @NotNull Question question,
            boolean translatedContent,
            boolean showTopics
    ) {
        StringBuilder content = new StringBuilder();
        content.append(CleanMarkdown.cleanMarkdown(
                translatedContent ? question.getTranslatedContent() : question.getContent(),
                ""
        ));
        appendTopics(content, question.getTopicTags(), showTopics);
        content.append("<div><li>👍 ")
                .append(question.getLikes())
                .append("</li><li>👎 ")
                .append(question.getDislikes())
                .append("</li></div>");
        return content.toString();
    }

    private static void appendTopics(StringBuilder content, List<Tag> topicTags, boolean showTopics) {
        if (!showTopics || topicTags == null || topicTags.isEmpty()) {
            return;
        }
        content.append("<div><div>Related Topics</div><div>");
        for (Tag tag : topicTags) {
            content.append("<li>")
                    .append(StringUtils.defaultIfBlank(tag.getTranslatedName(), tag.getName()))
                    .append("</li>");
        }
        content.append("</div></div><br>");
    }
}
