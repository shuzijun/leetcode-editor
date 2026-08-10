package com.shuzijun.leetcode.plugin.product;

import org.jetbrains.annotations.Nullable;

public interface ProductProfile {

    String marketplacePluginId();

    String pluginId();

    String pluginName();

    String actionPrefix();

    String actionSuffix();

    String notificationGroup();

    String toolWindowId();

    String consoleToolWindowId();

    String configurableId();

    String configurableDisplayName();

    String configNamespace();

    String fileExtension();

    String fileTypeName();

    String languageId();

    String protocolNamespace();

    String previewPathPrefix();

    String changelogUrl();

    @Nullable String convergeEditorTypeId();
}
