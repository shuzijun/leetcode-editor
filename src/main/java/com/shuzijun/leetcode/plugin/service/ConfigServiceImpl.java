package com.shuzijun.leetcode.plugin.service;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.repository.ConfigService;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;

public class ConfigServiceImpl implements ConfigService {

    public ConfigServiceImpl(Project project) {
    }
    @Override
    public void registerRepository(RepositoryService repositoryService) {
    }

    @Override
    public Config getConfig() {
        return PersistentConfig.getInstance().getInitConfig();
    }
}
