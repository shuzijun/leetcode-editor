package com.shuzijun.leetcode.plugin.spi;

import org.jetbrains.annotations.NotNull;

public interface LanguageTemplateProvider {

    @NotNull String fileName(@NotNull String languageSlug);

    @NotNull String template(@NotNull String languageSlug);
}
