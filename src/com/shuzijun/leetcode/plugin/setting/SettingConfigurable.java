package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class SettingConfigurable implements SearchableConfigurable {

    private SettingUI mainPanel;

    @NotNull
    @Override
    public String getId() {
        return "leetcode.id";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "leetcode plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "leetcode.helpTopic";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new SettingUI();
        mainPanel.createUI();
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
        mainPanel = null;
    }
}
