package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.spi.LanguageTemplateProvider;
import com.shuzijun.leetcode.plugin.utils.VelocityUtils;
import org.jetbrains.annotations.NotNull;

public final class LanguageTemplateService {

    private LanguageTemplateService() {
    }

    @NotNull
    public static String fileName(@NotNull String languageSlug, @NotNull Question question) {
        return fileName(ProductServices.languageTemplateProvider(), languageSlug, question);
    }

    @NotNull
    public static String template(@NotNull String languageSlug, @NotNull Question question) {
        return template(ProductServices.languageTemplateProvider(), languageSlug, question);
    }

    @NotNull
    static String fileName(
            @NotNull LanguageTemplateProvider provider,
            @NotNull String languageSlug,
            @NotNull Question question) {
        return VelocityUtils.convert(provider.fileName(languageSlug), question);
    }

    @NotNull
    static String template(
            @NotNull LanguageTemplateProvider provider,
            @NotNull String languageSlug,
            @NotNull Question question) {
        return VelocityUtils.convert(provider.template(languageSlug), question);
    }
}
