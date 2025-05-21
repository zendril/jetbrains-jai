package com.zendril.jetbrains.jai.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.zendril.jetbrains.jai.JailsLspService
import java.io.File

class JaiDebugAction : AnAction() {
    companion object {
        private val LOG = Logger.getInstance(JaiDebugAction::class.java)
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        
        // Use IDE's logger (will appear in idea.log)
        LOG.warn("JAI DEBUG ACTION EXECUTED")
        
        if (project == null) {
            LOG.warn("No project available")
            Messages.showErrorDialog("No project available", "Jai Debug")
            return
        }
        
        LOG.warn("Project: ${project.name}")

        // Create a notification regardless of service state
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Jai Notifications")
                ?.createNotification(
                    "Jai Debug Action", 
                    "Debug action executed for project: ${project.name}",
                    NotificationType.INFORMATION
                )
                ?.notify(project)
        } catch (ex: Exception) {
            LOG.error("Failed to create notification", ex)
        }
        
        // Attempt to get and use the JailsLspService
        val service = project.getService(JailsLspService::class.java)
        if (service != null) {
            LOG.warn("JailsLspService found for project: ${project.name}")

            try {
                // Test with a path that definitely exists (the project directory)
                val testPath = project.basePath
                LOG.warn("Project path: $testPath")

                // Check the hardcoded path
                val jailsPath = "C:\\Users\\kenne\\projects\\github\\SogoCZE\\Jails\\bin\\jails.exe"
                val jailsFile = File(jailsPath)
                
                val message = if (jailsFile.exists()) {
                    "Jails executable found at: $jailsPath"
                } else {
                    "Jails executable NOT found at: $jailsPath"
                }
                
                LOG.warn(message)
                Messages.showInfoMessage(message, "Jai Debug")
                
            } catch (ex: Exception) {
                LOG.error("Error in debug action", ex)
                ex.printStackTrace(System.out)
                Messages.showErrorDialog("Error: ${ex.message}", "Jai Debug Error")
            }
        } else {
            LOG.warn("JailsLspService is NULL for project: ${project.name}")
            Messages.showErrorDialog("JailsLspService not found for this project!", "Jai Debug")
        }
    }
}
