package com.zendril.jetbrains.jai.lsp.file

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class JaiFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(JaiFileType, JaiFileType.defaultExtension)
    }
}