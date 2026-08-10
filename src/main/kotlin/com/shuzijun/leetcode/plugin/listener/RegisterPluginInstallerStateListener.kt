package com.shuzijun.leetcode.plugin.listener

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.shuzijun.leetcode.plugin.model.PluginConstant
import com.shuzijun.leetcode.plugin.product.ProductProfiles
import com.shuzijun.leetcode.plugin.setting.PersistentConfig
import com.shuzijun.leetcode.plugin.utils.BrowserUtils
import com.shuzijun.leetcode.plugin.utils.PluginVersionUtils

/**
 * @author shuzijun
 */
class RegisterPluginInstallerStateListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        val newVersion = PluginVersionUtils.getVersion()
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
            openChangelog(project)
        }
    }

    private fun openChangelog(project: Project) {
        val profile = ProductProfiles.current()
        val notification = Notification(
            profile.notificationGroup(),
            "What's New in ${profile.pluginId()}",
            "A new version is available.",
            NotificationType.INFORMATION
        )
        notification.addAction(NotificationAction.createSimple("View changelog") {
            BrowserUtils.browse(profile.changelogUrl())
        })
        Notifications.Bus.notify(notification, project)
    }

    companion object {
        private val ShowNewHTMLEditorKey = PluginConstant.PLUGIN_ID + "ShowNewHTMLEditor"
    }

}
