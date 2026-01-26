package io.github.ackeecz.danger.testing.snapshot

import io.github.ackeecz.danger.testing.BuildFoldersMatcher
import io.github.ackeecz.danger.testing.MarkdownHeader
import java.io.File

/**
 * Configuration of UI snapshot tests processing.
 */
public class SnapshotConfig(
    public val deltaDiscovery: DeltaDiscovery = DeltaDiscovery(),
    public val deltaReport: DeltaReport = DeltaReport(),
) {

    /**
     * Configuration of failed snapshot delta report files discovery
     *
     * @param buildFoldersMatcher Allows to configure a matcher for build folders. Default is [BuildFoldersMatcher.All].
     * @param deltasFolderPath Allows to configure a path to the folder that contains delta files. This path
     * must be relative to the `build` directory. Defaults to "paparazzi".
     * @param deltaFileNamePrefix Allows to configure a prefix of the name of the delta file. Defaults to "delta-".
     * @param deltaFileExtension Allows to configure an extension of the delta file. Defaults to "png".
     */
    public class DeltaDiscovery(
        public val buildFoldersMatcher: BuildFoldersMatcher = BuildFoldersMatcher.All,
        public val deltasFolderPath: String = "paparazzi",
        public val deltaFileNamePrefix: String = "delta-",
        public val deltaFileExtension: String = "png",
    )

    /**
     * Configuration of failed snapshot delta report
     *
     * @param host GitLab host. Defaults to "gitlab.ack.ee".
     * @param projectGroupName GitLab project group name. Defaults to the value of the CI_PROJECT_NAMESPACE environment variable.
     * @param projectName GitLab project name. Defaults to the value of the CI_PROJECT_NAME environment variable.
     * @param snapshotJobIdFilePath Path to a file containing the ID of the snapshot test job. Defaults to the value
     * of the SNAPSHOT_TEST_JOB_ID_FILE environment variable.
     * @param failedSnapshotsHeader Markdown header to use for the failed snapshots report.
     * Defaults to "## ❌ Failed snapshots".
     */
    public class DeltaReport(
        internal val host: String = "gitlab.ack.ee",
        internal val projectGroupName: String = System.getenv("CI_PROJECT_NAMESPACE") ?: error("CI_PROJECT_NAMESPACE env not set"),
        internal val projectName: String = System.getenv("CI_PROJECT_NAME") ?: error("CI_PROJECT_NAME env not set"),
        snapshotJobIdFilePath: String = System.getenv("SNAPSHOT_TEST_JOB_ID_FILE") ?: error("SNAPSHOT_TEST_JOB_ID_FILE env not set"),
        internal val failedSnapshotsHeader: MarkdownHeader = MarkdownHeader(MarkdownHeader.Level.H2, "❌ Failed snapshots"),
    ) {

        internal val snapshotJobId: String = readSnapshotJobId(snapshotJobIdFilePath)

        private fun readSnapshotJobId(snapshotJobIdFilePath: String): String {
            return File(snapshotJobIdFilePath.trim()).readText().trim()
        }
    }
}
