package com.shuzijun.leetcode.extend.navigator;

import com.shuzijun.leetcode.extension.NavigatorPanel;
import com.shuzijun.leetcode.extension.NavigatorPanelFactory;
import com.shuzijun.leetcode.platform.RepositoryService;
import org.jetbrains.annotations.NotNull;

public class TreePanelFactory implements NavigatorPanelFactory {
    @Override
    public @NotNull String getName() {
        return "tree";
    }

    @Override
    public @NotNull NavigatorPanel createPanel(RepositoryService repositoryService) {
        return new TreePanel(repositoryService);
    }
}
