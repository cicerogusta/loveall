package com.loveall.app.fake

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.Message
import com.loveall.app.domain.repository.MessagesRepository
import com.loveall.app.fixtures.TestConversations
import com.loveall.app.fixtures.TestUsers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.*
import org.junit.Assert.*

internal class FakeMessagesRepository : MessagesRepository {
    var failSend = false
    var failLoad = false
    var refreshGate: CompletableDeferred<Unit>? = null
    var messageCalls = 0

    override suspend fun messages(matchId: String, page: Int): List<Message> {
        messageCalls++
        if (failLoad) throw AuthException(AuthError.NETWORK)
        refreshGate?.await()
        return listOf(
            Message(
                TestConversations.FIRST_MESSAGE_ID,
                TestUsers.PARTNER_ID,
                TestConversations.INCOMING_TEXT,
                TestConversations.FIRST_CREATED_AT,
                true,
            )
        )
    }

    override suspend fun send(matchId: String, content: String): Message {
        if (failSend) throw AuthException(AuthError.NETWORK)
        return Message(
            TestConversations.SECOND_MESSAGE_ID,
            TestUsers.SELF_ID,
            content,
            TestConversations.SECOND_CREATED_AT,
            false,
        )
    }
}
