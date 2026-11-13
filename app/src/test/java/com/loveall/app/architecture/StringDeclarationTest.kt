package com.loveall.app.architecture

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class StringDeclarationTest {
    @Test
    fun `production strings belong to constants enum entries or Android resources`() {
        // Arrange
        val sources =
            listOf(File("src/main/java"), File("app/src/main/java")).first(File::isDirectory)
        val stringLiteral = Regex("""["](?:\\.|[^"\\])*["]""")
        val enumEntry = Regex("""^[A-Z][A-Z_]+\(""")

        // Act
        val violations =
            sources
                .walkTopDown()
                .filter { it.extension == "kt" }
                .flatMap { file ->
                    val lines = file.readLines()
                    lines.mapIndexedNotNull { index, line ->
                        val trimmed = line.trim()
                        val comment =
                            trimmed.startsWith("//") ||
                                trimmed.startsWith("*") ||
                                trimmed.startsWith("/*")
                        val declaration =
                            line.contains("const val ") || enumEntry.containsMatchIn(trimmed)
                        val continuedDeclaration =
                            index > 0 &&
                                lines[index - 1].contains("const val ") &&
                                lines[index - 1].trimEnd().endsWith('=')
                        if (
                            !comment &&
                                !declaration &&
                                !continuedDeclaration &&
                                stringLiteral.containsMatchIn(line)
                        )
                            "${file.relativeTo(sources)}:${index + 1}: $trimmed"
                        else null
                    }
                }
                .toList()

        // Assert
        assertTrue(violations.joinToString(System.lineSeparator()), violations.isEmpty())
    }
}
