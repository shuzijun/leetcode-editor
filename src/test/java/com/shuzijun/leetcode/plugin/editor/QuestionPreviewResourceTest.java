package com.shuzijun.leetcode.plugin.editor;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class QuestionPreviewResourceTest {

    @Test
    public void templateRendersAllReadOnlyContentThroughVditorPreview() throws Exception {
        String template = resource("/template/default.html");

        assertTrue(template.contains("\"after\": function ()"));
        assertTrue(template.contains("{{previewReady}}"));
        assertTrue(template.contains("{{previewStable}}"));
        assertTrue(template.contains("{{previewError}}"));
        assertTrue(template.contains("function loadVditor()"));
        assertTrue(template.contains("document.head.appendChild(script)"));
        assertTrue(template.contains("function renderPreview()"));
        assertTrue(template.contains("document.readyState === \"loading\""));
        assertTrue(template.contains("renderPreview();"));
        assertTrue(template.contains("Vditor.preview(previewElement"));
        assertTrue(template.contains("document.getElementById(\"fileValue\").value"));
        assertTrue(template.contains("data-render-mode=\"{{renderMode}}\""));
        assertTrue(template.contains("<link rel=\"stylesheet\""));
        assertFalse(template.contains("previewElement.innerHTML"));
        assertFalse(template.contains("Vditor.codeRender(previewElement)"));
        assertFalse(template.contains("scheduleEnhancement"));
        assertFalse(template.contains("<script src=\"{{service}}resources/vditor"));
        assertFalse(template.contains("<script defer src=\"{{service}}resources/vditor"));
        assertFalse(template.contains("jquery-3.6.0.min.js"));
    }

    @Test
    public void escapesTextareaTerminationWithoutChangingMarkdownSemantics() {
        assertEquals(
                "before &lt;/textarea&gt;&amp; after",
                LCVPanel.escapeTextarea("before </textarea>& after")
        );
    }

    private static String resource(String path) throws Exception {
        try (InputStream input = QuestionPreviewResourceTest.class.getResourceAsStream(path)) {
            assertNotNull(input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
