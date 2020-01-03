package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class RegisterPluginInstallerStateListener implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
       /* PluginInstaller.addStateListener(new PluginStateListener() {
            @Override
            public void install(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {
            }

            @Override
            public void uninstall(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {
                System.out.println("uninstall");
            }
        });*/
    }
}
