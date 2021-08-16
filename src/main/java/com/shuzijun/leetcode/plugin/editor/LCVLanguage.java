package com.shuzijun.leetcode.plugin.editor;

import com.intellij.lang.Language;
import com.shuzijun.leetcode.plugin.model.PluginConstant;

public class LCVLanguage extends Language {

    public static final String LANGUAGE_NAME = PluginConstant.LEETCODE_EDITOR_VIEW+"Doc";

    public static final LCVLanguage INSTANCE = new LCVLanguage();

    protected LCVLanguage() {
        super(LANGUAGE_NAME);
    }
}
