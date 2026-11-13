package com.loveall.app.fake

import com.loveall.app.domain.model.BlockedUser
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.domain.model.SafetyTip
import com.loveall.app.domain.repository.SafetyRepository
import com.loveall.app.fixtures.TestUsers
import kotlinx.coroutines.test.*
import org.junit.Assert.*

internal class FakeSafetyRepository : SafetyRepository {
    override suspend fun tips() = emptyList<SafetyTip>()

    override suspend fun blocked() =
        listOf(BlockedUser(TestUsers.PARTNER_ID, TestUsers.PARTNER_NAME))

    override suspend fun block(id: String) {}

    override suspend fun unblock(id: String) {}

    override suspend fun report(id: String, reason: ReportReason, description: String) {}
}
