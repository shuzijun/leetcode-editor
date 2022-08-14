package com.shuzijun.leetcode.extension;

import com.shuzijun.leetcode.platform.RepositoryService;
import org.jetbrains.annotations.NotNull;

public interface NavigatorPanelFactory {

    @NotNull String getName();

    @NotNull NavigatorPanel createPanel(RepositoryService repositoryService);
}
