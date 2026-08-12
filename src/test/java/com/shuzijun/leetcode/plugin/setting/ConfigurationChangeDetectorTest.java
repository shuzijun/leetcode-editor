package com.shuzijun.leetcode.plugin.setting;

import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.CustomCode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationChangeDetectorTest {

    @Test
    public void reportsNoChangeForEquivalentStateAndPassword() {
        assertFalse(ConfigurationChangeDetector.hasChanged(
                config("leetcode.cn", "Java", false),
                config("leetcode.cn", "Java", false),
                "secret",
                "secret"
        ));
    }

    @Test
    public void reportsFieldAndPasswordChanges() {
        Config saved = config("leetcode.cn", "Java", false);

        assertTrue(ConfigurationChangeDetector.hasChanged(
                config("leetcode.com", "Java", false), saved, "secret", "secret"));
        assertTrue(ConfigurationChangeDetector.hasChanged(
                config("leetcode.cn", "Kotlin", false), saved, "secret", "secret"));
        assertTrue(ConfigurationChangeDetector.hasChanged(
                config("leetcode.cn", "Java", false), saved, "new-secret", "secret"));
    }

    @Test
    public void reportsLanguageAndTemplateChanges() {
        Config saved = config("leetcode.cn", "Java", true);
        saved.setEnglishContent(false);
        saved.setCustomFileName("Answer");
        saved.setCustomTemplate("class Answer {}");
        saved.addCustomCode("java", new CustomCode("java", "Answer", "class Answer {}"));

        Config languageChanged = config("leetcode.cn", "Java", true);
        languageChanged.setEnglishContent(true);
        languageChanged.setCustomFileName("Answer");
        languageChanged.setCustomTemplate("class Answer {}");
        languageChanged.addCustomCode("java", new CustomCode("java", "Answer", "class Answer {}"));

        Config legacyTemplateChanged = config("leetcode.cn", "Java", true);
        legacyTemplateChanged.setCustomFileName("Solution");
        legacyTemplateChanged.setCustomTemplate("class Solution {}");
        legacyTemplateChanged.addCustomCode("java", new CustomCode("java", "Answer", "class Answer {}"));

        Config proTemplateChanged = config("leetcode.cn", "Java", true);
        proTemplateChanged.setCustomFileName("Answer");
        proTemplateChanged.setCustomTemplate("class Answer {}");
        proTemplateChanged.addCustomCode("java", new CustomCode("java", "Solution", "class Solution {}"));

        assertTrue(ConfigurationChangeDetector.hasChanged(
                languageChanged, saved, "secret", "secret"));
        assertTrue(ConfigurationChangeDetector.hasChanged(
                legacyTemplateChanged, saved, "secret", "secret"));
        assertTrue(ConfigurationChangeDetector.hasChanged(
                proTemplateChanged, saved, "secret", "secret"));
    }

    @Test
    public void reportsRestoredStateAsUnchangedAndFirstConfigurationAsChanged() {
        Config saved = config("leetcode.cn", "Java", false);
        Config restored = config("leetcode.cn", "Kotlin", false);
        restored.setCodeType("Java");

        assertFalse(ConfigurationChangeDetector.hasChanged(
                restored, saved, "secret", "secret"));
        assertTrue(ConfigurationChangeDetector.hasChanged(
                restored, null, "secret", null));
    }

    private static Config config(String site, String codeType, boolean customCode) {
        Config config = new Config();
        config.setUrl(site);
        config.setCodeType(codeType);
        config.setCustomCode(customCode);
        return config;
    }
}
