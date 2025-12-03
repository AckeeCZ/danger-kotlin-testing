package io.github.ackeecz.danger.testing.junit

import io.github.ackeecz.danger.testing.BuildFoldersMatcher

/**
 * Configuration of JUnit tests reports finding and processing.
 */
public class JUnitConfig(
    public val discovery: Discovery = Discovery(),
) {

    /**
     * Configuration of JUnit report files discovery
     *
     * @param buildFoldersMatcher Allows to configure a matcher for build folders. Default is [BuildFoldersMatcher.All].
     * @param testResultsFolderPath Allows to configure a path to the folder that contains test results. This path
     * must be relative to the `build` directory. Defaults to "test-results".
     * @param resultFileNamePrefix Allows to configure a prefix of the name of the test result file. Defaults to "TEST-".
     */
    public class Discovery(
        public val buildFoldersMatcher: BuildFoldersMatcher = BuildFoldersMatcher.All,
        public val testResultsFolderPath: String = "test-results",
        public val resultFileNamePrefix: String = "TEST-"
    )
}
