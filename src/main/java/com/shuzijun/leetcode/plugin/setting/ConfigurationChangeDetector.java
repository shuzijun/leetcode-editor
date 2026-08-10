package com.shuzijun.leetcode.plugin.setting;

import com.shuzijun.leetcode.plugin.model.Config;

import java.util.Objects;

public final class ConfigurationChangeDetector {

    private ConfigurationChangeDetector() {
    }

    public static boolean hasChanged(Config current, Config saved,
                                     String currentPassword, String savedPassword) {
        return saved == null
                || !current.isModified(saved)
                || !Objects.equals(currentPassword, savedPassword);
    }

    public static boolean languageChanged(Config previous, Config current) {
        return !Objects.equals(previous.getEnglishContent(), current.getEnglishContent())
                || !Objects.equals(previous.getCodeType(), current.getCodeType());
    }

    public static boolean templateChanged(Config previous, Config current) {
        return !Objects.equals(previous.getCustomCode(), current.getCustomCode())
                || !Objects.equals(previous.getCustomCodes(), current.getCustomCodes());
    }
}
