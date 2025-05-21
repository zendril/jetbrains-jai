package com.zendril.jetbrains.jai.file

import com.intellij.lang.Language

class JaiLanguage private constructor() : Language("Jai") {
    companion object {
        val INSTANCE = JaiLanguage()
    }
}