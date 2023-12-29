package org.gitea.git

import org.apache.commons.codec.digest.DigestUtils
import java.util.*

data class Commit(
    val tree: Tree,
    val author: String,
    val message: String,
    val parent: Commit? = null,
    val commitTime: Date = Date(),
    val hash: String = calculateHash(tree.hash + author + message + parent?.hash + commitTime)
) {
    companion object {
        private fun calculateHash(data: String): String {
            return DigestUtils.sha1Hex(data)
        }
    }

    fun printContent() {
        println("Message: $message")
        println("Author: $author")
        println("Date: $commitTime")
        println("Hash: $hash")
    }
}