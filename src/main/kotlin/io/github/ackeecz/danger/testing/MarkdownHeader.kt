package io.github.ackeecz.danger.testing

public class MarkdownHeader(
    public val level: Level,
    public val text: String,
) {

    internal fun toMarkdownString(): String = "#".repeat(level.value) + " $text"

    public enum class Level(internal val value: Int) {

        H1(value = 1),
        H2(value = 2),
        H3(value = 3),
        H4(value = 4),
        H5(value = 5),
        H6(value = 6),
    }
}
