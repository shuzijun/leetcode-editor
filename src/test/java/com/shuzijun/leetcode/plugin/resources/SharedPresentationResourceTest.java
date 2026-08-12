package com.shuzijun.leetcode.plugin.resources;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SharedPresentationResourceTest {

    @Test
    public void keepsAnsiResultMessagesInBothLanguages() throws Exception {
        Properties english = properties("/i18n/info.properties");
        Properties chinese = properties("/i18n/info_zh.properties");

        assertTrue(english.getProperty("submit.success").contains("\033[1;32m"));
        assertTrue(english.getProperty("submit.failed").contains("\033[1;31m"));
        assertTrue(chinese.getProperty("submit.success").contains("\033[1;32m"));
        assertTrue(chinese.getProperty("submit.failed").contains("\033[1;31m"));
        assertTrue(english.containsKey("codetop.config.load"));
        assertTrue(chinese.containsKey("codetop.config.load"));
    }

    @Test
    public void usesReadOnlyVditorPreviewResources() throws Exception {
        String template = resource("/template/default.html");
        String style = resource("/vditor/style.css");

        assertTrue(template.contains("Vditor.preview("));
        assertFalse(template.contains("new Vditor("));
        assertTrue(style.contains(".vditor-reset table"));
    }

    private static Properties properties(String path) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = SharedPresentationResourceTest.class.getResourceAsStream(path)) {
            assertNotNull(input);
            properties.load(input);
        }
        return properties;
    }

    private static String resource(String path) throws Exception {
        try (InputStream input = SharedPresentationResourceTest.class.getResourceAsStream(path)) {
            assertNotNull(input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
