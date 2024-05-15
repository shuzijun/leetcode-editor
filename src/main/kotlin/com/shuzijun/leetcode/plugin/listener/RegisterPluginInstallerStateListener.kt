package com.shuzijun.leetcode.plugin.listener

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.shuzijun.leetcode.plugin.model.PluginConstant
import com.shuzijun.leetcode.plugin.setting.PersistentConfig

/**
 * @author shuzijun
 */
class RegisterPluginInstallerStateListener : StartupActivity {
    override fun runActivity(project: Project) {
        val newVersion = getPlugin(PluginId.getId(PluginConstant.PLUGIN_ID))!!.version
        val config = PersistentConfig.getInstance().initConfig
        val oldVersion: String?
        if (config == null) {
            oldVersion = PropertiesComponent.getInstance()
                .getValue(ShowNewHTMLEditorKey)
            PropertiesComponent.getInstance()
                .setValue(ShowNewHTMLEditorKey, newVersion)
        } else {
            oldVersion = config.pluginVersion
            config.pluginVersion = newVersion
        }

        if (newVersion != oldVersion) {
            HTMLEditorProvider.openEditor(
                project,
                "What's New in " + PluginConstant.PLUGIN_ID,
                CHANGELOGURL,
                "'<div style='text-align: center;padding-top: 3rem'>" +
                        "<div style='padding-top: 1rem; margin-bottom: 0.8rem;'>Failed to load!</div>" +
                        "'<div><a href='" + CHANGELOGURL + "' target='_blank'" +
                        "style='font-size: 2rem'>Open in browser</a></div>" +
                        "</div>"
            )
        }
    }

    companion object {
        private const val ShowNewHTMLEditorKey = PluginConstant.PLUGIN_ID + "ShowNewHTMLEditor"

        private const val CHANGELOGURL = "https://github.com/shuzijun/leetcode-editor/blob/master/CHANGELOG.md"
    }

}
