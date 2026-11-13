package com.loveall.app.fake

import com.loveall.app.TestFixtures
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.Match
import com.loveall.app.domain.repository.ConnectionsRepository
import com.loveall.app.fixtures.TestConversations
import kotlinx.coroutines.test.*
import org.junit.Assert.*

internal class FakeConnectionsRepository : ConnectionsRepository {
    var unexpectedFailure = false
    var failLike = false
    var likeCalls = 0

    override suspend fun like(id: String, superLike: Boolean): String? {
        likeCalls++
        if (unexpectedFailure) throw IllegalStateException(MALFORMED_MATCH_MESSAGE)
        if (failLike) throw AuthException(AuthError.NETWORK)
        return TestConversations.MATCH_ID
    }

    override suspend fun pass(id: String) {}

    override suspend fun likes() = listOf(TestFixtures.profile)

    override suspend fun matches() =
        listOf(Match(TestConversations.MATCH_ID, TestFixtures.profile, null, 0))

    override suspend fun unmatch(id: String) {}
}

private const val MALFORMED_MATCH_MESSAGE = "Malformed match"
