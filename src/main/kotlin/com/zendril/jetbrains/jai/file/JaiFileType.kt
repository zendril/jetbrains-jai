package com.zendril.jetbrains.jai.file

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object JaiFileType : LanguageFileType(JaiLanguage.INSTANCE) {
    override fun getName(): String = "Jai"
    override fun getDescription(): String = "Jai language file"
    override fun getDefaultExtension(): String = "jai"
    override fun getIcon(): Icon = IconLoader.getIcon("/icons/jai.png", JaiFileType::class.java)
}