package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface LicenseGate {

    boolean isAllowed();

    void onDenied();

    void beforeAction(@NotNull Project project);
}
