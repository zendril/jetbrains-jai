package com.zendril.jetbrains.jai

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.ProjectActivity
import java.io.File

class JaiProjectOpenActivity : ProjectActivity {
    companion object {
        // Use both custom logger and standard Logger for maximum visibility
        private val LOG = Logger.getInstance(JaiProjectOpenActivity::class.java)
    }


    init {
        println("===== JAI PROJECT OPEN ACTIVITY INSTANTIATED =====")
        System.out.println("===== JAI PROJECT OPEN ACTIVITY INSTANTIATED =====")
    }

    // ProjectActivity interface changed in recent IntelliJ versions.
    // For modern IDEs (2022.3+), this is the correct signature.
    // For older ones, it might be `override fun runActivity(project: Project)`.
    // `execute` is a suspend function if you need coroutine context.
    override suspend fun execute(project: Project) {
        // Log through both loggers
        LOG.info("JaiProjectOpenActivity executing for project: ${project.name}")

        try {
            // Display a notification to confirm activity execution
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Jai Notifications")
                ?.createNotification(
                    "Jai Project Activity",
                    "Jai plugin activated for project: ${project.name}",
                    NotificationType.INFORMATION
                )
                ?.notify(project)

            LOG.info("Notification displayed for project: ${project.name}")
        } catch (e: Exception) {
            e.printStackTrace(System.out)
            LOG.error("Error showing notification", e)
        }

        // Verify that we can access all open projects
        val openProjects = ProjectManager.getInstance().openProjects
        LOG.info("Open projects from JaiProjectOpenActivity: ${openProjects.joinToString { it.name }}")

        val jailsLspService = project.getService(JailsLspService::class.java)
        if (jailsLspService != null) {
            LOG.info("Found JailsLspService, preparing to start Jails server")

            // TODO: Make the path to "jails" configurable.
            // For now, you might hardcode it or use an environment variable.
            val jailsExecutablePath = "C:\\Users\\kenne\\projects\\github\\SogoCZE\\Jails\\bin\\jails.exe"
            LOG.info("Using Jails executable path: $jailsExecutablePath")

            // Check if the executable exists
            val jailsFile = File(jailsExecutablePath)
            if (jailsFile.exists()) {
                LOG.info("Jails executable found at: $jailsExecutablePath")
            } else {
                LOG.warn("Jails executable NOT found at: $jailsExecutablePath")
            }

            // You might want to check if this is a "Jai" project before starting.
            // For example, check for specific project files or module types.
            jailsLspService.startServer(jailsExecutablePath)
        } else {
            LOG.warn("JailsLspService not found! Cannot start Jails server.")
        }
    }
}