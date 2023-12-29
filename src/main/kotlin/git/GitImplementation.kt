package org.gitea.git

class GitImplementation {
    var headCommit: Commit? = null
        private set
    private var futureHeadTree = Tree("", emptyList())
    private var commits: MutableList<Commit> = mutableListOf()

    fun add(entry: TreeEntry) {
        if (futureHeadTree.findEntry(entry.path) == null) {
            futureHeadTree.addEntry(entry.path, entry)
            return
        }
        futureHeadTree.changeEntry(entry.path, entry)
    }

    fun commit(message: String, author: String) {
        val commit = Commit(futureHeadTree, author, message, headCommit)
        futureHeadTree = Tree("", futureHeadTree.entries)
        headCommit = commit
        commits.add(commit)
    }

    fun listCommits(): List<Commit> {
        val commits = mutableListOf<Commit>()
        var currentCommit = headCommit
        while (currentCommit!= null) {
            commits.add(currentCommit)
            currentCommit = currentCommit.parent
        }
        return commits
    }

    fun findCommit(hash: String): Commit? {
        return commits.find { it.hash == hash }
    }

    fun findCommit(message: String, author: String): Commit? {
        return commits.find { it.message == message && it.author == author }
    }
}