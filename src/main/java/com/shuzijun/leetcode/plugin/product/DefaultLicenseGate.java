package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class DefaultLicenseGate implements LicenseGate {

    @Override
    public boolean isAllowed() {
        return true;
    }

    @Override
    public void onDenied() {
    }

    @Override
    public void beforeAction(@NotNull Project project) {
    }
}
