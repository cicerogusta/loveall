package com.loveall.app.presentation

import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class MessageTimeTest {
    private val locale = Locale.forLanguageTag("pt-BR")
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun `SQLite and ISO timestamps represent the same UTC instant`() {
        val sqlite = formatMessageTime("2026-11-12 12:00:00", locale, zone)
        assertEquals(sqlite, formatMessageTime("2026-11-12T12:00:00Z", locale, zone))
        assertTrue(sqlite!!.contains("09:00"))
    }

    @Test
    fun `invalid timestamps are not exposed as technical text`() {
        assertNull(formatMessageTime("invalid-date", locale, zone))
    }
}
