package com.zendril.jetbrains.jai.lsp.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class JaiHelloAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInfoMessage("Hello from Jai Language Support Plugin!", "Jai Plugin")
    }
}