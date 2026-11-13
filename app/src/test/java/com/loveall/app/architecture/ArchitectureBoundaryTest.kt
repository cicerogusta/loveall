package com.loveall.app.architecture

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureBoundaryTest {
    private val sources =
        listOf(File("src/main/java/com/loveall/app"), File("app/src/main/java/com/loveall/app"))
            .first { it.isDirectory }

    @Test
    fun `domain remains independent of Android presentation and data implementations`() {
        assertImports(
            "domain",
            listOf(
                "android.",
                "androidx.",
                "retrofit2.",
                "okhttp3.",
                "com.loveall.app.data.",
                "com.loveall.app.presentation.",
                "com.loveall.app.ui.",
            ),
        )
    }

    @Test
    fun `viewmodels depend on domain contracts and never on UI or transport`() {
        assertImports(
            "presentation",
            listOf(
                "android.",
                "retrofit2.",
                "okhttp3.",
                "com.loveall.app.data.",
                "com.loveall.app.ui.",
            ),
        )
        File(sources, "presentation")
            .walkTopDown()
            .filter { it.name.endsWith("ViewModel.kt") }
            .forEach { file ->
                val declarations =
                    Regex(
                            "(?m)^(?:data |enum |sealed |abstract )*(?:class|interface|object) (\\w+)"
                        )
                        .findAll(file.readText())
                        .map { it.groupValues[1] }
                        .toList()
                assertTrue(
                    "${file.name} must only declare its ViewModel",
                    declarations == listOf(file.nameWithoutExtension),
                )
                assertTrue(
                    "${file.name} must extend AndroidX ViewModel directly",
                    Regex("\\)\\s*:\\s*ViewModel\\(\\)").containsMatchIn(file.readText()),
                )
            }
    }

    @Test
    fun `screens and visual components do not access storage network or repository implementations`() {
        assertImports("ui/screens", listOf("com.loveall.app.data.", "retrofit2.", "okhttp3."))
        assertImports(
            "ui/components",
            listOf(
                "com.loveall.app.data.",
                "retrofit2.",
                "okhttp3.",
                "com.loveall.app.ui.screens.",
            ),
        )
    }

    private fun assertImports(directory: String, forbidden: List<String>) {
        File(sources, directory)
            .walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                file
                    .readLines()
                    .filter { it.startsWith("import ") }
                    .forEach { line ->
                        val imported = line.removePrefix("import ")
                        assertTrue(
                            "${file.relativeTo(sources)} violates a layer boundary: $line",
                            forbidden.none(imported::startsWith),
                        )
                    }
            }
    }
}
