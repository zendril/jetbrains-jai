package com.zendril.jetbrains.jai.lsp.file

import com.intellij.lang.Language

class JaiLanguage private constructor() : Language("Jai") {
    companion object {
        val INSTANCE = JaiLanguage()
    }
}