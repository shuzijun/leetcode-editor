package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface CookieLoginStrategy {

    void login(@NotNull Project project, @NotNull String cookies);

    @NotNull
    default CompletableFuture<Boolean> loginAsync(
            @NotNull Project project,
            @NotNull String cookies
    ) {
        login(project, cookies);
        return CompletableFuture.completedFuture(true);
    }
}
