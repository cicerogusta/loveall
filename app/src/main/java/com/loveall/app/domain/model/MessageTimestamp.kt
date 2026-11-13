package com.loveall.app.domain.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object MessageTimestamp {
    private const val SQLITE_PATTERN = "yyyy-MM-dd HH:mm:ss"
    private val sqlite = DateTimeFormatter.ofPattern(SQLITE_PATTERN, Locale.ROOT)

    fun parse(value: String): Instant? = runCatching {
        Instant.parse(value)
    }
        .getOrElse {
            runCatching { LocalDateTime.parse(value, sqlite).toInstant(ZoneOffset.UTC) }.getOrNull()
        }
}
