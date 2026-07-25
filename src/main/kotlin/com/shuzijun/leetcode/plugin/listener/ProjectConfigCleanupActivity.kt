package com.shuzijun.leetcode.plugin.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.shuzijun.leetcode.plugin.setting.ProjectConfig

/** Keeps the per-project editor index small without doing filesystem I/O on the UI thread. */
class ProjectConfigCleanupActivity : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        ApplicationManager.getApplication().executeOnPooledThread {
            if (!project.isDisposed) {
                ProjectConfig.getInstance(project)?.pruneStaleEntries(project.basePath)
            }
        }
    }
}
