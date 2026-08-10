package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import com.shuzijun.leetcode.plugin.application.LeetCodeApplicationService;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.spi.SettingsSectionProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class SettingConfigurable implements SearchableConfigurable {

    private final List<SettingsSectionProvider.SettingsSection> sections = new ArrayList<>();
    private com.intellij.openapi.Disposable sectionDisposable;

    @NotNull
    @Override
    public String getId() {
        return ProductProfiles.current().configurableId();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return ProductProfiles.current().configurableDisplayName();
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
        disposeSections();
        sectionDisposable = Disposer.newDisposable("LeetCode settings sections");
        List<SettingsSectionProvider> providers =
                LeetCodeApplicationService.getInstance().settingsSections();
        if (providers.isEmpty()) {
            throw new IllegalStateException("No settings sections are registered");
        }
        for (SettingsSectionProvider provider : providers) {
            sections.add(provider.createSection(sectionDisposable));
        }
        if (sections.size() == 1) {
            return sections.get(0).getComponent();
        }
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        for (SettingsSectionProvider.SettingsSection section : sections) {
            container.add(section.getComponent());
        }
        return container;
    }

    @Override
    public boolean isModified() {
        return sections.stream().anyMatch(SettingsSectionProvider.SettingsSection::isModified);
    }

    @Override
    public void apply() throws ConfigurationException {
        for (SettingsSectionProvider.SettingsSection section : sections) {
            section.apply();
        }
    }

    @Override
    public void reset() {
        for (SettingsSectionProvider.SettingsSection section : sections) {
            section.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        disposeSections();
    }

    private void disposeSections() {
        sections.clear();
        if (sectionDisposable != null) {
            Disposer.dispose(sectionDisposable);
            sectionDisposable = null;
        }
    }

}
