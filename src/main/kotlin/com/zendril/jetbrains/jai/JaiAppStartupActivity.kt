package com.zendril.jetbrains.jai

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.StartupActivity

class JaiAppStartupActivity : StartupActivity {
    companion object {
        private val LOG = Logger.getInstance(JaiAppStartupActivity::class.java)
    }

    override fun runActivity(project: Project) {
        LOG.info("Jai plugin startup activity running for project: ${project.name}")
        
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Jai Notifications")
                ?.createNotification(
                    "Jai plugin startup activity executed", 
                    "Project: ${project.name}",
                    NotificationType.INFORMATION
                )
                ?.notify(project)
        } catch (e: Exception) {
            LOG.error("Failed to show notification from startup activity: ${e.message}")
            e.printStackTrace(System.out)
        }

        // Verify that we can access all open projects
        val openProjects = ProjectManager.getInstance().openProjects
        LOG.debug("Open projects: ${openProjects.joinToString { it.name }}")
        
        // Verify JailsLspService can be obtained
        for (openProject in openProjects) {
            val service = openProject.getService(JailsLspService::class.java)
            if (service != null) {
                LOG.info("Found JailsLspService for project: ${openProject.name}")
            } else {
                LOG.warn("JailsLspService is NULL for project: ${openProject.name}")
            }
        }
    }
}
