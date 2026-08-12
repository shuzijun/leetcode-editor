package com.shuzijun.leetcode.plugin.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigTemplateStateTest {

    @Test
    public void usesLegacyDefaultsWhenCustomTemplatesAreDisabled() {
        Config config = new Config();
        config.setCustomCode(false);

        assertEquals(Constant.CUSTOM_FILE_NAME, config.getCustomFileName("java"));
        assertEquals(Constant.CUSTOM_TEMPLATE, config.getCustomTemplate("java"));
        assertEquals("", config.getCustomTemplate("note"));
        assertEquals("", config.getCustomTemplate("content"));
    }

    @Test
    public void storesCustomTemplatesIndependentlyByLanguage() {
        Config config = new Config();
        config.setCustomCode(true);
        config.addCustomCode("java", new CustomCode("java", "JavaAnswer", "class JavaAnswer {}"));
        config.addCustomCode("python3", new CustomCode("python3", "python_answer", "def solve(): pass"));

        assertEquals("JavaAnswer", config.getCustomFileName("java"));
        assertEquals("class JavaAnswer {}", config.getCustomTemplate("java"));
        assertEquals("python_answer", config.getCustomFileName("python3"));
        assertEquals("def solve(): pass", config.getCustomTemplate("python3"));
    }

    @Test
    public void includesLanguageTemplatesWhenComparingConfigurationState() {
        Config saved = new Config();
        saved.setCustomCode(true);
        saved.addCustomCode("java", new CustomCode("java", "Answer", "class Answer {}"));

        Config unchanged = new Config();
        unchanged.setCustomCode(true);
        unchanged.addCustomCode("java", new CustomCode("java", "Answer", "class Answer {}"));

        Config changed = new Config();
        changed.setCustomCode(true);
        changed.addCustomCode("java", new CustomCode("java", "Answer", "class Changed {}"));

        assertTrue(unchanged.isModified(saved));
        assertFalse(changed.isModified(saved));
    }
}
