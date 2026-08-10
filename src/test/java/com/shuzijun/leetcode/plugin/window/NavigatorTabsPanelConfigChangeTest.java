package com.shuzijun.leetcode.plugin.window;

import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.CustomCode;
import com.shuzijun.leetcode.plugin.setting.ConfigurationChangeDetector;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NavigatorTabsPanelConfigChangeTest {

    @Test
    public void detectsContentAndCodeLanguageChanges() {
        Config saved = config("Java", false);
        Config contentLanguage = config("Java", true);
        Config codeLanguage = config("Kotlin", false);

        assertTrue(ConfigurationChangeDetector.languageChanged(saved, contentLanguage));
        assertTrue(ConfigurationChangeDetector.languageChanged(saved, codeLanguage));
        assertFalse(ConfigurationChangeDetector.languageChanged(saved, config("Java", false)));
    }

    @Test
    public void detectsTemplateChangesWithoutTreatingLanguageAsTemplateState() {
        Config saved = config("Java", false);
        saved.setCustomCode(true);
        saved.addCustomCode("java", new CustomCode("java", "Answer", "class Answer {}"));

        Config changed = config("Java", false);
        changed.setCustomCode(true);
        changed.addCustomCode("java", new CustomCode("java", "Solution", "class Solution {}"));

        assertTrue(ConfigurationChangeDetector.templateChanged(saved, changed));
        assertFalse(ConfigurationChangeDetector.templateChanged(saved, saved.clone()));
        assertFalse(ConfigurationChangeDetector.templateChanged(saved, configWithLanguage(saved, "Kotlin")));
    }

    private static Config config(String codeType, boolean englishContent) {
        Config config = new Config();
        config.setCodeType(codeType);
        config.setEnglishContent(englishContent);
        config.setCustomCode(false);
        return config;
    }

    private static Config configWithLanguage(Config source, String codeType) {
        Config changed = source.clone();
        changed.setCodeType(codeType);
        return changed;
    }
}
