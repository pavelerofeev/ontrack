import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Before
import java.io.ByteArrayOutputStream
    private lateinit var oldPrefix: String
    private lateinit var newPrefix: String

    @Before
    fun `Load user git diff config`() {
        val userGitDiffFormat = DiffFormatter(ByteArrayOutputStream())
        userGitDiffFormat.setRepository(FileRepository(""))
        oldPrefix = userGitDiffFormat.oldPrefix
        newPrefix = userGitDiffFormat.newPrefix
    }

diff --git ${oldPrefix}file1 ${newPrefix}file1
--- ${oldPrefix}file1
+++ ${newPrefix}file1
diff --git ${oldPrefix}file2 ${newPrefix}file2
--- ${oldPrefix}file2
+++ ${newPrefix}file2
diff --git ${oldPrefix}file3 ${newPrefix}file3
+++ ${newPrefix}file3
diff --git ${oldPrefix}file4 ${newPrefix}file4
--- ${oldPrefix}file4
diff --git ${oldPrefix}file1 ${newPrefix}file1
--- ${oldPrefix}file1
+++ ${newPrefix}file1