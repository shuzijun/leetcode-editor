package com.shuzijun.leetcode.plugin.setting;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.CustomCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersistentConfigTest {

    @Test
    public void migratesLegacyCustomTemplateIntoTheVersionFourLanguageMap() {
        Config legacyConfig = legacyConfig("Java");
        PersistentConfig persistentConfig = new PersistentConfig();
        persistentConfig.setInitConfig(legacyConfig);

        Config migratedConfig = persistentConfig.getInitConfig();

        assertEquals(Constant.PLUGIN_CONFIG_VERSION_4, migratedConfig.getVersion());
        String langSlug = CodeTypeEnum.getCodeTypeEnum("Java").getLangSlug();
        CustomCode migratedCustomCode = migratedConfig.getCustomCode(langSlug);
        assertEquals("LegacyAnswer", migratedCustomCode.getFileName());
        assertEquals("class LegacyAnswer {}", migratedCustomCode.getTemplate());
        assertEquals(langSlug, migratedCustomCode.getLangSlug());
    }

    @Test
    public void completesVersionFourMigrationWhenLegacyLanguageIsUnknown() {
        Config legacyConfig = legacyConfig("RemovedLanguage");
        PersistentConfig persistentConfig = new PersistentConfig();
        persistentConfig.setInitConfig(legacyConfig);

        Config migratedConfig = persistentConfig.getInitConfig();

        assertEquals(Constant.PLUGIN_CONFIG_VERSION_4, migratedConfig.getVersion());
        assertEquals(0, migratedConfig.getCustomCodes().size());
        assertEquals("LegacyAnswer", migratedConfig.getCustomFileName());
        assertEquals("class LegacyAnswer {}", migratedConfig.getCustomTemplate());
    }

    private static Config legacyConfig(String codeType) {
        Config config = new Config();
        config.setVersion(Constant.PLUGIN_CONFIG_VERSION_3);
        config.setUrl("leetcode.cn");
        config.setCodeType(codeType);
        config.setCustomCode(true);
        config.setCustomFileName("LegacyAnswer");
        config.setCustomTemplate("class LegacyAnswer {}");
        return config;
    }
}
