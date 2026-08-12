package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;

@Service(Service.Level.PROJECT)
public final class DefaultMessageUtils extends MessageUtils {

    public DefaultMessageUtils(Project project) {
        super(project);
    }
}
