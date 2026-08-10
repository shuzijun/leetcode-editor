package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public interface SettingsSectionProvider extends OrderedContribution {

    @NotNull SettingsSection createSection(@NotNull Disposable parentDisposable);

    interface SettingsSection {

        @NotNull JComponent getComponent();

        boolean isModified();

        void apply();

        void reset();
    }
}
