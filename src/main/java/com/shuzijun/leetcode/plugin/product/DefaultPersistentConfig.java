package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;

@State(
        name = "PersistentConfig",
        storages = @Storage(value = "leetcode-config.xml", roamingType = RoamingType.DISABLED)
)
public final class DefaultPersistentConfig extends PersistentConfig {
}
