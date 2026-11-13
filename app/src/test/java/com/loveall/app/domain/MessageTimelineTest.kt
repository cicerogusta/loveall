package com.loveall.app.domain

import com.loveall.app.domain.model.Message
import com.loveall.app.domain.usecase.MessageTimeline
import com.loveall.app.fixtures.TestConversations
import org.junit.Assert.*
import org.junit.Test

class MessageTimelineTest {
    @Test
    fun `timeline sorts SQLite and ISO timestamps by instant and deduplicates messages`() {
        val early = Message("early", "sender", "Earlier", "2026-11-12T09:00:00Z", false)
        val late = Message("late", "sender", "Later", TestConversations.FIRST_CREATED_AT, true)
        val result = MessageTimeline.merge(listOf(late, early), listOf(late.copy(read = false)))
        assertEquals(listOf("early", "late"), result.map(Message::id))
        assertTrue(result.last().read)
    }

    @Test
    fun `equal or invalid timestamps produce a stable order`() {
        val b = Message("b", "sender", "B", "invalid", false)
        val a = b.copy(id = "a")
        assertEquals(listOf("a", "b"), MessageTimeline.merge(listOf(b), listOf(a)).map(Message::id))
    }
}
