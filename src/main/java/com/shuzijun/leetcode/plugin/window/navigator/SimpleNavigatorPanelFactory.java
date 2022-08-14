package com.shuzijun.leetcode.plugin.window.navigator;

import com.shuzijun.leetcode.extension.NavigatorPanel;
import com.shuzijun.leetcode.extension.NavigatorPanelFactory;
import com.shuzijun.leetcode.platform.RepositoryService;
import org.jetbrains.annotations.NotNull;

public class SimpleNavigatorPanelFactory implements NavigatorPanelFactory {
    @Override
    public @NotNull String getName() {
        return "page";
    }

    @Override
    public @NotNull NavigatorPanel createPanel(RepositoryService repositoryService) {
        return new SimpleNavigatorPanel(repositoryService);
    }
}
