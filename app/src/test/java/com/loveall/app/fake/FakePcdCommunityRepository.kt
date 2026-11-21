package com.loveall.app.fake

import com.loveall.app.TestFixtures
import com.loveall.app.domain.repository.PcdCommunityRepository
import com.loveall.app.fixtures.TestConversations
import java.io.IOException

internal class FakePcdCommunityRepository : PcdCommunityRepository {
    var member = false
    var discoverCalls = 0
    var mutationCalls = 0
    var likeCalls = 0
    var failLike = false
    var failMutation = false

    override suspend fun isMember() = member

    override suspend fun setMembership(enabled: Boolean, consent: Boolean): Boolean {
        mutationCalls++
        if (failMutation) throw IOException()
        check(!enabled || consent)
        member = enabled
        return member
    }

    override suspend fun discover() = listOf(TestFixtures.profile).also { discoverCalls++ }

    override suspend fun like(id: String): String? {
        likeCalls++
        if (failLike) throw IOException()
        return TestConversations.MATCH_ID
    }
}
