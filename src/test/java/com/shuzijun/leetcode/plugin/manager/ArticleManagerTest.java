package com.shuzijun.leetcode.plugin.manager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArticleManagerTest {

    @Test
    public void restoresMarkdownThatWasEscapedAsOneLine() {
        String escaped = "# Intuition\\nThe explanation.\\n\\n# Code\\n```java\\n"
                + "String newline = \"\\\\n\";\\n```";

        assertEquals(
                "# Intuition\nThe explanation.\n\n# Code\n```java\n"
                        + "String newline = \"\\n\";\n```",
                ArticleManager.normalizeEscapedMarkdown(escaped)
        );
    }

    @Test
    public void preservesEscapesInNormallyFormattedMarkdown() {
        String markdown = "# Code\n```java\nString newline = \"\\n\";\n```";

        assertEquals(markdown, ArticleManager.normalizeEscapedMarkdown(markdown));
    }

    @Test
    public void preservesAnIsolatedLiteralEscape() {
        String content = "Use \\n to represent a newline.";

        assertEquals(content, ArticleManager.normalizeEscapedMarkdown(content));
    }
}
