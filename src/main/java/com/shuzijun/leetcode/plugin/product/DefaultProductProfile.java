package com.shuzijun.leetcode.plugin.product;

import org.jetbrains.annotations.Nullable;

public final class DefaultProductProfile implements ProductProfile {

    @Override
    public String marketplacePluginId() {
        return "12132";
    }

    @Override
    public String pluginId() {
        return "leetcode-editor";
    }

    @Override
    public String pluginName() {
        return "leetcode.editor";
    }

    @Override
    public String actionPrefix() {
        return "leetcode";
    }

    @Override
    public String actionSuffix() {
        return "";
    }

    @Override
    public String notificationGroup() {
        return "leetcode editor";
    }

    @Override
    public String toolWindowId() {
        return "Leetcode";
    }

    @Override
    public String consoleToolWindowId() {
        return "Leetcode Console";
    }

    @Override
    public String configurableId() {
        return "leetcode.id";
    }

    @Override
    public String configurableDisplayName() {
        return "LeetCode Plugin";
    }

    @Override
    public String configNamespace() {
        return "leetcode";
    }

    @Override
    public String fileExtension() {
        return "lcv";
    }

    @Override
    public String fileTypeName() {
        return "lcvDoc";
    }

    @Override
    public String languageId() {
        return "lcvDoc";
    }

    @Override
    public String protocolNamespace() {
        return "leetcode-editor";
    }

    @Override
    public String previewPathPrefix() {
        return "/leetcode/";
    }

    @Override
    public String changelogUrl() {
        return "https://github.com/shuzijun/leetcode-editor/blob/master/CHANGELOG.md";
    }

    @Override
    public @Nullable String convergeEditorTypeId() {
        return null;
    }
}
