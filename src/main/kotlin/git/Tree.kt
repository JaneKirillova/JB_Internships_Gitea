package org.gitea.git

import org.apache.commons.codec.digest.DigestUtils


class Tree(path: String, entries: List<TreeEntry>) :
    TreeEntry(path, calculateHash(entries.joinToString { it.path + it.hash })) {
    companion object {
        private fun calculateHash(entries: String): String {
            return DigestUtils.sha1Hex(entries)
        }
    }

    private val entriesMap: MutableMap<String, TreeEntry> = entries.associateBy { it.path }.toMutableMap()

    val entries: List<TreeEntry>
        get() = entriesMap.values.toList()

    internal fun findEntry(name: String): TreeEntry? {
        val pathElements = name.split("/")
        if (pathElements.size == 1) {
            return entriesMap[name]
        }
        val tree = entriesMap[pathElements[0]] ?: return null
        if (tree !is Tree) return null
        return tree.findEntry(pathElements.drop(1).joinToString("/"))
    }

    internal fun addEntry(name: String, entry: TreeEntry) {
        val pathElements = name.split("/")
        if (pathElements.size == 1) {
            entriesMap[entry.path] = entry
            return
        }
        val tree = entriesMap[pathElements[0]] ?: Tree(pathElements[0], listOf())
        if (tree !is Tree) throw GitException("Cannot add entry to non-tree")
        tree.addEntry(pathElements.drop(1).joinToString("/"), entry)
        entriesMap[pathElements[0]] = tree
    }

    internal fun changeEntry(name: String, newEntry: TreeEntry) {
        val pathElements = name.split("/")
        if (pathElements.size == 1) {
            entriesMap[newEntry.path] = newEntry
            return
        }
        val tree = entriesMap[pathElements[0]] ?: throw GitException("Something is wrong, entry $name not found")
        if (tree !is Tree) throw GitException("Cannot change entry in non-tree")
        tree.changeEntry(pathElements.drop(1).joinToString("/"), newEntry)
    }

    override fun printContent(prefix: String) {
        println("${prefix}Tree: $path, hash: $hash, children:")
        entriesMap.values.forEach { it.printContent("${prefix}\t") }
    }

}
