package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.project.Project;
import com.shuzijun.lc.model.User;
import org.jetbrains.annotations.NotNull;

public interface NavigatorSessionStrategy {

    @NotNull String displayName(@NotNull User user);

    void afterLogin(@NotNull Project project);
}
