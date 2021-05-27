package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class SettingConfigurable implements SearchableConfigurable {

    public static final String DISPLAY_NAME = PluginConstant.APPLICATION_CONFIGURABLE_DISPLAY_NAME;

    private SettingUI mainPanel;

    @NotNull
    @Override
    public String getId() {
        return PluginConstant.APPLICATION_CONFIGURABLE_ID;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return PluginConstant.APPLICATION_CONFIGURABLE_DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "leetcode.helpTopic";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new SettingUI();
        return mainPanel.getContentPane();
    }

    @Override
    public boolean isModified() {
        return mainPanel.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        mainPanel.apply();
    }

    @Override
    public void reset() {
        mainPanel.reset();
    }

    @Override
    public void disposeUIResources() {
        mainPanel.disposeUIResources();
        mainPanel = null;
    }

}
