package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.project.Project;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.spi.NavigatorSessionStrategy;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public final class DefaultNavigatorSessionStrategy implements NavigatorSessionStrategy {

    @Override
    public @NotNull String displayName(@NotNull User user) {
        return StringUtils.defaultString(user.getUsername());
    }

    @Override
    public void afterLogin(@NotNull Project project) {
    }
}
