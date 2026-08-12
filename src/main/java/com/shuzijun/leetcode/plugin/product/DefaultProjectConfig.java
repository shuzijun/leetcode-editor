package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;

@State(name = "LeetcodeEditor", storages = @Storage("leetcode/editor.xml"))
public final class DefaultProjectConfig extends ProjectConfig {
}
