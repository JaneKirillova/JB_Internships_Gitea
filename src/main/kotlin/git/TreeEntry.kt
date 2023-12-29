package org.gitea.git

abstract class TreeEntry(val path: String, val hash: String) {
    abstract fun printContent(prefix: String = "")
}