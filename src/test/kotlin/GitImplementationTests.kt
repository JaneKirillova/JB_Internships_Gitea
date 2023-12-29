import org.gitea.git.Blob
import org.gitea.git.GitException
import org.gitea.git.GitImplementation
import org.gitea.git.Tree
import org.junit.jupiter.api.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.Thread.sleep

class GitImplementationTests {
    private val outputStream = ByteArrayOutputStream()
    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outputStream))
    }

    @AfterEach
    fun setDown() {
        System.setOut(System.out)
    }


    @Test
    fun testCommitOneBlob() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit1", "author")
        Assertions.assertNotNull(git.headCommit)
        Assertions.assertEquals(1, git.headCommit!!.tree.entries.size)
        Assertions.assertTrue(git.headCommit!!.tree.entries[0] is Blob)
        Assertions.assertEquals("Blob 1", (git.headCommit!!.tree.entries[0] as Blob).data)

        git.headCommit!!.tree.printContent()
        val printedOutput = outputStream.toString().trim()
        val expectedOutput = """
        Tree: , hash: da39a3ee5e6b4b0d3255bfef95601890afd80709, children:
        ${"\t"}Blob: blob1, hash: 69d5375a62877b1158a83bc2722d1968452dc6e1, data: Blob 1
        """.trimIndent()
        Assertions.assertEquals(expectedOutput, printedOutput)
    }

    @Test
    fun testCommitNewTree() {
        val git = GitImplementation()
        git.add(Tree("tree1", listOf(Blob("blob1", "Blob 1"))))
        git.commit("commit1", "author")
        Assertions.assertNotNull(git.headCommit)
        Assertions.assertEquals(1, git.headCommit!!.tree.entries.size)
        Assertions.assertTrue(git.headCommit!!.tree.entries[0] is Tree)
        Assertions.assertEquals(1, (git.headCommit!!.tree.entries[0] as Tree).entries.size)
        val blob1 = (git.headCommit!!.tree.entries[0] as Tree).entries[0] as Blob
        Assertions.assertEquals("Blob 1", blob1.data)

        git.headCommit!!.tree.printContent()
        val printedOutput = outputStream.toString().trim()
        val expectedOutput = """
        Tree: , hash: da39a3ee5e6b4b0d3255bfef95601890afd80709, children:
        ${"\t"}Tree: tree1, hash: 242224cb5ca0e82ad7f8cc333d5bad76239ab31e, children:
        ${"\t"}${"\t"}Blob: blob1, hash: 69d5375a62877b1158a83bc2722d1968452dc6e1, data: Blob 1
        """.trimIndent()
        Assertions.assertEquals(expectedOutput, printedOutput)
    }

    @Test
    fun testCommitSameBlobWithNewData() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.add(Blob("blob2", "Blob 2"))
        git.commit("commit1", "author")

        val blob2FirstCommit = git.headCommit!!.tree.entries[1] as Blob

        git.add(Blob("blob1", "New blob 1"))
        git.commit("commit2", "author")

        Assertions.assertEquals(2, git.headCommit!!.tree.entries.size)
        Assertions.assertTrue(git.headCommit!!.tree.entries[0] is Blob)
        Assertions.assertEquals("New blob 1", (git.headCommit!!.tree.entries[0] as Blob).data)
        val blob2SecondCommit = git.headCommit!!.tree.entries[1] as Blob
        Assertions.assertEquals(blob2SecondCommit.hash, blob2FirstCommit.hash)

        git.headCommit!!.tree.printContent()
        val printedOutput = outputStream.toString().trim()
        val expectedOutput = """
        Tree: , hash: 29b0938cc4d48b411e04bbd70ba68289f229046d, children:
        ${"\t"}Blob: blob1, hash: 1608b715b2797740f3f82323a8c3e66864ceb789, data: New blob 1
        ${"\t"}Blob: blob2, hash: 1df2eb9b23108b3947cc111d24af3dcbc0220b3b, data: Blob 2
        """.trimIndent()
        Assertions.assertEquals(expectedOutput, printedOutput)
    }

    @Test
    fun testCommitSameTreeWithNewData() {
        val git = GitImplementation()
        git.add(Tree("tree1", listOf(Blob("blob1", "Blob 1"))))
        git.commit("commit1", "author")

        git.add(Tree("tree1", listOf(Blob("blob2", "Blob 2"), Blob("blob3", "Blob 3"))))
        git.commit("commit2", "author")

        Assertions.assertEquals(1, git.headCommit!!.tree.entries.size)
        Assertions.assertTrue(git.headCommit!!.tree.entries[0] is Tree)
        val tree1 = (git.headCommit!!.tree.entries[0] as Tree)
        val entries = tree1.entries
        Assertions.assertFalse(entries.any {
            (it as Blob).data == "Blob 1"
        })

        git.headCommit!!.tree.printContent()
        val printedOutput = outputStream.toString().trim()
        val expectedOutput = """
        Tree: , hash: 78acbc82b01ba01324f824fb06ae1460b6cbdf81, children:
        ${"\t"}Tree: tree1, hash: dc9b868ebc2355d9c82e9298af3d882782d3d31e, children:
        ${"\t"}${"\t"}Blob: blob2, hash: 1df2eb9b23108b3947cc111d24af3dcbc0220b3b, data: Blob 2
        ${"\t"}${"\t"}Blob: blob3, hash: a1c4efd737de2fcfcd9ff5933e0fef3eb1d9f8e1, data: Blob 3
        """.trimIndent()
        Assertions.assertEquals(expectedOutput, printedOutput)
    }

    @Test
    fun testCommitTreeFromAnotherCommit() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.add(Blob("blob2", "Blob 2"))
        git.commit("commit1", "author")

        git.add(Blob("blob3", "Blob 3"))
        git.commit("commit2", "author")

        git.add(git.headCommit!!.parent!!.tree)
        git.commit("commit3", "author")

        Assertions.assertEquals(4, git.headCommit!!.tree.entries.size)
        Assertions.assertTrue(git.headCommit!!.tree.entries[3] is Tree)
        val tree = (git.headCommit!!.tree.entries[3] as Tree)

        Assertions.assertEquals(2, tree.entries.size)
        Assertions.assertEquals("Blob 1", (tree.entries[0] as Blob).data)
        Assertions.assertEquals("Blob 2", (tree.entries[1] as Blob).data)

        git.headCommit!!.tree.printContent()
        val printedOutput = outputStream.toString().trim()
        val expectedOutput = """
        Tree: , hash: d4f9f19556b6ba826b9bd361d9e95503dccce5c7, children:
        ${"\t"}Blob: blob1, hash: 69d5375a62877b1158a83bc2722d1968452dc6e1, data: Blob 1
        ${"\t"}Blob: blob2, hash: 1df2eb9b23108b3947cc111d24af3dcbc0220b3b, data: Blob 2
        ${"\t"}Blob: blob3, hash: a1c4efd737de2fcfcd9ff5933e0fef3eb1d9f8e1, data: Blob 3
        ${"\t"}Tree: , hash: da39a3ee5e6b4b0d3255bfef95601890afd80709, children:
        ${"\t"}${"\t"}Blob: blob1, hash: 69d5375a62877b1158a83bc2722d1968452dc6e1, data: Blob 1
        ${"\t"}${"\t"}Blob: blob2, hash: 1df2eb9b23108b3947cc111d24af3dcbc0220b3b, data: Blob 2
        """.trimIndent()
        Assertions.assertEquals(expectedOutput, printedOutput)
    }

    @Test
    fun testExceptionOnAddingBlobToNotTree() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        val exception = Assertions.assertThrows(GitException::class.java) {
            git.add(Blob("blob1/blob2", "Blob 2"))
        }
        val expectedMessage = "Cannot add entry to non-tree"
        Assertions.assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testListCommits() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 1", "author 1")
        sleep(100)
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 2", "author 2")
        sleep(100)
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 3", "author 3")

        val commits = git.listCommits()
        Assertions.assertEquals(3, commits.size)
        Assertions.assertEquals("commit 3", commits[0].message)
        Assertions.assertEquals("author 3", commits[0].author)
        Assertions.assertEquals("commit 2", commits[1].message)
        Assertions.assertEquals("author 2", commits[1].author)
        Assertions.assertEquals("commit 1", commits[2].message)
        Assertions.assertEquals("author 1", commits[2].author)
        Assertions.assertTrue(commits[0].commitTime > commits[1].commitTime)
        Assertions.assertTrue(commits[1].commitTime > commits[2].commitTime)
    }

    @Test
    fun testFindCommit() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 1", "author 1")
        sleep(100)
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 2", "author 2")
        sleep(100)
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 3", "author 3")

        val commit = git.findCommit(git.headCommit!!.hash)
        Assertions.assertNotNull(commit)
        Assertions.assertEquals("commit 3", commit!!.message)
        Assertions.assertEquals("author 3", commit.author)
    }

    @Test
    fun testFindCommitNotFound() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 1", "author 1")
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 2", "author 2")

        val commit = git.findCommit("123")
        Assertions.assertNull(commit)
    }

    @Test
    fun testFindCommitByMetadata() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 1", "author 1")
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 2", "author 2")
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 3", "author 3")

        val commit = git.findCommit("commit 2", "author 2")
        Assertions.assertNotNull(commit)
        Assertions.assertEquals("commit 2", commit!!.message)
        Assertions.assertEquals("author 2", commit.author)
    }

    @Test
    fun testFindCommitByMetadataNotFound() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 1", "author 1")
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 2", "author 2")
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 3", "author 3")

        val commit = git.findCommit("commit 2", "author 3")
        Assertions.assertNull(commit)
    }

    @Test
    fun testPrintCommitContent() {
        val git = GitImplementation()
        git.add(Blob("blob1", "Blob 1"))
        git.commit("commit 1", "author 1")
        git.headCommit!!.printContent()
        val printedOutput = outputStream.toString().trim()
        Assertions.assertTrue(printedOutput.contains("Message: commit 1"))
        Assertions.assertTrue(printedOutput.contains("Author: author 1"))
        Assertions.assertTrue(printedOutput.contains("Date: "))
        Assertions.assertTrue(printedOutput.contains("Hash: "))
    }
}