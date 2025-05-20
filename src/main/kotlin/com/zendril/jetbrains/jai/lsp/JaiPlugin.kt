package com.zendril.jetbrains.jai.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

// Option 1: Using StartupActivity (Recommended for most cases)
class MyProjectOpenActivity : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        // This is effectively your new "projectOpened"
        println("Project opened: ${project.name}")
        // Add your logic here

        // If you need to do something when the project closes,
        // you can register a an application-level listener or a project-level disposable
        // to clean up or perform actions.
        // For project closing, you might subscribe to ProjectManager.TOPIC with a different listener
        // or use a ProjectCloseListener if that suits your needs better,
        // though often cleanup is handled via Disposables.
    }
}


// Somewhere in your plugin initialization, likely for a specific project,
// or if you need to handle all projects, you might connect at the application level.

// For a specific project, e.g., in a ProjectComponent's projectOpened (itself deprecated)
// or within a StartupActivity:
// val project: Project = ... // get current project
// project.messageBus.connect().subscribe(ProjectManager.TOPIC, MyProjectListener(project))

// If you truly need to listen to *all* project open/close events at an application level:
// ApplicationManager.getApplication().messageBus.connect(myPluginDisposable)
// .subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
// override fun projectOpened(project: Project) {
// println("Any project opened: ${project.name}")
// }
//
// override fun projectClosed(project: Project) {
// println("Any project closed: ${project.name}")
// }
// })
