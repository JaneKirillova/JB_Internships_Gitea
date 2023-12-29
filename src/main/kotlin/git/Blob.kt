package org.gitea.git

import org.apache.commons.codec.digest.DigestUtils

class Blob(npath: String, val data: String) : TreeEntry(npath, calculateHash(data)) {

    companion object {
        private fun calculateHash(data: String): String {
            return DigestUtils.sha1Hex(data)
        }
    }

    override fun printContent(prefix: String) {
        println("${prefix}Blob: $path, hash: $hash, data: $data")
    }
}