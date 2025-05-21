package com.zendril.jetbrains.jai

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Scanner
import java.util.concurrent.TimeUnit

class JailsLspService(private val project: Project) : Disposable {

    companion object {
        private val LOG = Logger.getInstance(JailsLspService::class.java)
    }

    init {
        LOG.info("JailsLspService created for project: ${project.name}")
    }

    private var jailsProcessHandler: ProcessHandler? = null
    private var jailsProcess: Process? = null
    private var jailsStdin: OutputStream? = null

    fun startServer(jailsExecutablePath: String) {
        LOG.info("Starting Jails LSP server for project: ${project.name}")
        
        if (jailsProcessHandler?.isProcessTerminated == false || jailsProcess?.isAlive == true) {
            LOG.warn("Jails LSP server already running for project: ${project.name}")
            return
        }
    
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                LOG.info("Starting Jails LSP server from: $jailsExecutablePath")
                
                val jailsFile = File(jailsExecutablePath)
                if (!jailsFile.exists()) {
                    val errorMsg = "Jails executable file does not exist: $jailsExecutablePath"
                    LOG.error(errorMsg)
                    
                    // Show notification on UI thread
                    ApplicationManager.getApplication().invokeLater {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Jai Notifications")
                            ?.createNotification(
                                "Jails Executable Not Found", 
                                errorMsg,
                                NotificationType.ERROR
                            )
                            ?.notify(project)
                    }
                    return@executeOnPooledThread
                }
                
                // Using ProcessBuilder for more direct control over the process streams
                val processBuilder = ProcessBuilder(jailsExecutablePath)

                LOG.info("Starting Jails process...")
                
                jailsProcess = processBuilder.start()
                jailsStdin = jailsProcess?.outputStream
    
                // Wrap the process in an IntelliJ ProcessHandler for easier management
                // and to integrate with IntelliJ's process handling (e.g. termination listener)
                val commandLine = GeneralCommandLine(jailsExecutablePath) // Used by OSProcessHandler for command line string
                jailsProcess?.let { proc ->
                    LOG.info("Wrapping Jails process with ProcessHandler")
                    
                    jailsProcessHandler = OSProcessHandler(proc, commandLine.preparedCommandLine)
                    ProcessTerminatedListener.attach(jailsProcessHandler!!)
                    jailsProcessHandler?.startNotify()
                    
                    val pidMessage = "Jails LSP server started. PID: ${proc.pid()}"
                    LOG.info(pidMessage)
                    
                    startStreamReaders(proc)
                    
                    // Show successful notification
                    ApplicationManager.getApplication().invokeLater {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Jai Notifications")
                            ?.createNotification(
                                "Jails Server Started", 
                                "LSP server started for ${project.name}. PID: ${proc.pid()}",
                                NotificationType.INFORMATION
                            )
                            ?.notify(project)
                    }
                } ?: run {
                    val errorMsg = "Failed to start Jails process: process is null"
                    LOG.error(errorMsg)
                    
                    // Show notification on UI thread
                    ApplicationManager.getApplication().invokeLater {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Jai Notifications")
                            ?.createNotification(
                                "Jails Server Error", 
                                errorMsg,
                                NotificationType.ERROR
                            )
                            ?.notify(project)
                    }
                }
    
                LOG.info("Jails server startup complete")
    
            } catch (e: Exception) {
                val errorMsg = "Failed to start Jails LSP server: ${e.message}"
                e.printStackTrace(System.out)
                LOG.error("Failed to start Jails LSP server", e)
                
                // Show notification on UI thread
                ApplicationManager.getApplication().invokeLater {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Jai Notifications")
                        ?.createNotification(
                            "Jails Server Error", 
                            errorMsg,
                            NotificationType.ERROR
                        )
                        ?.notify(project)
                }
            }
        }
    }

    private fun startStreamReaders(process: Process) {
        LOG.info("Setting up stream readers for Jails process")
        
        // Simplified example: just log output.
        // In a real LSP client, stdout is for JSON-RPC messages, stderr for server logs.
        Thread({
            try {
                LOG.info("Starting stdout reader for Jails process")
                Scanner(process.inputStream, StandardCharsets.UTF_8.name()).use { scanner ->
                    while (scanner.hasNextLine()) {
                        val line = scanner.nextLine()
                        LOG.info("[Jails STDOUT] $line")
                        // Here you would parse LSP messages
                    }
                }
            } catch (e: IOException) {
                if (!process.isAlive && e.message?.contains("Stream closed", ignoreCase = true) == true) {
                    LOG.info("Jails STDOUT stream closed as process terminated.")
                } else {
                    LOG.error("Error reading Jails STDOUT", e)
                }
            } catch (e: Exception) {
                LOG.error("Exception in Jails STDOUT reader", e)
            }
        }, "Jails LSP stdout reader").apply {
            isDaemon = true
            start()
        }

        Thread({
            try {
                LOG.info("Starting stderr reader for Jails process")
                Scanner(process.errorStream, StandardCharsets.UTF_8.name()).use { scanner ->
                    while (scanner.hasNextLine()) {
                        val line = scanner.nextLine()
                        LOG.warn("[Jails STDERR] $line")
                    }
                }
            } catch (e: IOException) {
                 if (!process.isAlive && e.message?.contains("Stream closed", ignoreCase = true) == true) {
                    LOG.info("Jails STDERR stream closed as process terminated.")
                } else {
                    LOG.error("Error reading Jails STDERR", e)
                }
            } catch (e: Exception) {
                LOG.error("Exception in Jails STDERR reader", e)
            }
        }, "Jails LSP stderr reader").apply {
            isDaemon = true
            start()
        }
    }

    fun stopServer() {
        LOG.info("Stopping Jails LSP server for project: ${project.name}")
        
        if (jailsProcess == null && (jailsProcessHandler == null || jailsProcessHandler?.isProcessTerminated == true)) {
            LOG.info("Jails LSP server not running or already terminated.")
            return
        }

        LOG.info("Preparing to stop Jails LSP server")

        // Graceful shutdown (LSP specific) - Placeholder
        // You'd send LSP 'shutdown' and 'exit' messages via jailsStdin here.
        // Example:
        // if (jailsStdin != null) {
        //     try {
        //         val shutdownRequest = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"shutdown\"}\r\n"
        //         val exitNotification = "{\"jsonrpc\":\"2.0\",\"method\":\"exit\"}\r\n"
        //         LOG.info("Sending shutdown request to Jails server...")
        //         jailsStdin?.write(shutdownRequest.toByteArray(StandardCharsets.UTF_8))
        //         jailsStdin?.flush()
        //         // Wait for response or timeout
        //         LOG.info("Sending exit notification to Jails server...")
        //         jailsStdin?.write(exitNotification.toByteArray(StandardCharsets.UTF_8))
        //         jailsStdin?.flush()
        //     } catch (e: IOException) {
        //         LOG.warn("Error sending shutdown/exit to Jails server", e)
        //     }
        // }

        jailsProcessHandler?.let {
            if (!it.isProcessTerminated) {
                LOG.info("Destroying process handler")
                it.destroyProcess() // Sends SIGTERM
            }
        }

        jailsProcess?.let {
            if (it.isAlive) {
                try {
                    LOG.info("Waiting for Jails server to exit gracefully...")
                    val exited = it.waitFor(5, TimeUnit.SECONDS)
                    if (!exited) {
                        LOG.warn("Jails server did not exit gracefully, forcing termination.")
                        it.destroyForcibly() // Sends SIGKILL
                    }
                } catch (e: InterruptedException) {
                    LOG.warn("Interrupted while waiting for Jails server to exit, forcing termination.", e)
                    if (it.isAlive) {
                        it.destroyForcibly()
                    }
                    Thread.currentThread().interrupt()
                }
            }
        }
        
        try {
            LOG.info("Closing Jails input stream")
            jailsStdin?.close()
        } catch (e: IOException) {
            LOG.error("Error closing Jails server stdin", e)
        }

        jailsProcess = null
        jailsProcessHandler = null
        jailsStdin = null
        LOG.info("Jails LSP server stopped successfully.")
    }

    override fun dispose() {
        LOG.info("Disposing JailsLspService for project: ${project.name}")
        stopServer()
    }
}
