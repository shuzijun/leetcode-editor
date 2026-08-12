package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.spi.LanguageTemplateProvider;
import org.jetbrains.annotations.NotNull;

public final class DefaultLanguageTemplateProvider implements LanguageTemplateProvider {

    @Override
    public @NotNull String fileName(@NotNull String languageSlug) {
        return PersistentConfig.getInstance().getConfig().getCustomFileName();
    }

    @Override
    public @NotNull String template(@NotNull String languageSlug) {
        return PersistentConfig.getInstance().getConfig().getCustomTemplate();
    }
}
