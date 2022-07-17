package com.shuzijun.leetcode.plugin.listener;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class RegisterPluginInstallerStateListener implements StartupActivity {

    private final static String ShowNewHTMLEditorKey = PluginConstant.PLUGIN_ID + "ShowNewHTMLEditor";

    private final static String CHANGELOGURL = "https://github.com/shuzijun/leetcode-editor/blob/master/CHANGELOG.md";

    @Override
    public void runActivity(@NotNull Project project) {
        String newVersion = PluginManagerCore.getPlugin(PluginId.getId(PluginConstant.PLUGIN_ID)).getVersion();
        Config config = PersistentConfig.getInstance().getInitConfig();
        String oldVersion;
        if (config == null) {
            oldVersion = PropertiesComponent.getInstance().getValue(ShowNewHTMLEditorKey);
            PropertiesComponent.getInstance().setValue(ShowNewHTMLEditorKey, newVersion);
        } else {
            oldVersion = config.getPluginVersion();
            config.setPluginVersion(newVersion);
        }

        if (!newVersion.equals(oldVersion)) {
            HTMLEditorProvider.openEditor(project,
                    "What's New in " + PluginConstant.PLUGIN_ID,
                    CHANGELOGURL,
                    "'<div style='text-align: center;padding-top: 3rem'>" +
                            "<div style='padding-top: 1rem; margin-bottom: 0.8rem;'>Failed to load!</div>" +
                            "'<div><a href='" + CHANGELOGURL + "' target='_blank'" +
                            "style='font-size: 2rem'>Open in browser</a></div>" +
                            "</div>"
            );
        }


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
