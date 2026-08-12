package com.shuzijun.leetcode.plugin.application;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.spi.ContributionRegistry;
import com.shuzijun.leetcode.plugin.spi.EditorTabContribution;
import com.shuzijun.leetcode.plugin.spi.NavigatorContribution;
import com.shuzijun.leetcode.plugin.spi.QuestionCatalogProvider;
import com.shuzijun.leetcode.plugin.spi.SettingsSectionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LeetCodeApplicationService {

    public static LeetCodeApplicationService getInstance() {
        return LeetCodeServices.application();
    }

    @NotNull
    public QuestionCatalogProvider catalog(@NotNull String sourceId) {
        List<QuestionCatalogProvider> providers = extensionList("questionCatalog");
        for (QuestionCatalogProvider provider : providers) {
            if (provider.supports(sourceId)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No question catalog provider for " + sourceId);
    }

    @NotNull
    public List<NavigatorContribution> navigators() {
        return ContributionRegistry.ordered(extensionList("navigatorContribution"));
    }

    @NotNull
    public List<EditorTabContribution> editorTabs() {
        return ContributionRegistry.ordered(extensionList("editorTabContribution"));
    }

    @NotNull
    public List<SettingsSectionProvider> settingsSections() {
        return ContributionRegistry.ordered(extensionList("settingsSection"));
    }

    @NotNull
    private <T> List<T> extensionList(@NotNull String localName) {
        String extensionPointName = ProductServices.profile().pluginId() + "." + localName;
        return ExtensionPointName.<T>create(extensionPointName).getExtensionList();
    }
}
