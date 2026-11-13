package com.loveall.app.fixtures

internal object JsonFixture {
    fun read(path: String): String =
        requireNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
                MISSING_FIXTURE_MESSAGE + path
            }
            .bufferedReader()
            .use { it.readText() }

    private const val MISSING_FIXTURE_MESSAGE = "Missing JSON fixture: "
}
