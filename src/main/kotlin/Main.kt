package org.gitea

import org.gitea.git.Blob
import org.gitea.git.GitImplementation
import org.gitea.git.Tree

fun main() {
    val git = GitImplementation()
    git.add(Blob("blob1", "Blob 1"))
    git.add(Blob("blob2", "Blob 2"))
    git.commit("commit1", "author")

    git.add(Blob("tree2/blob1", "Blob 2r430239"))
    git.add(Tree("tree3", emptyList()))
    git.add(Blob("blob1", "Blob 123"))
    git.commit("commit2", "author")

    println("Content of commit 2:")
    git.headCommit!!.printContent()

    println("Tree of commit 2:")
    git.headCommit!!.tree.printContent()
}