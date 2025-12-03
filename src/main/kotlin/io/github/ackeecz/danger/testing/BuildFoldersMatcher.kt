package io.github.ackeecz.danger.testing

/**
 * Matcher for build folders used during file discovery
 */
public sealed interface BuildFoldersMatcher {

    /**
     * Matches all build folders in the project
     */
    public object All : BuildFoldersMatcher

    /**
     * Matches specific build folders in the project. Paths must be relative to the root project directory.
     */
    public class Specific(public vararg val paths: String) : BuildFoldersMatcher
}
