package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.shuzijun.leetcode.plugin.adapter.defaults.setting.SettingUI;
import com.shuzijun.leetcode.plugin.spi.SettingsSectionProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public final class DefaultSettingsSectionProvider implements SettingsSectionProvider {

    @Override
    public @NotNull String getId() {
        return "default";
    }

    @Override
    public @NotNull SettingsSection createSection(@NotNull Disposable parentDisposable) {
        SettingUI settings = new SettingUI();
        Disposer.register(parentDisposable, settings::disposeUIResources);
        return new SettingsSection() {
            @Override
            public @NotNull JComponent getComponent() {
                return settings.getContentPane();
            }

            @Override
            public boolean isModified() {
                return settings.isModified();
            }

            @Override
            public void apply() {
                settings.apply();
            }

            @Override
            public void reset() {
                settings.reset();
            }
        };
    }
}
