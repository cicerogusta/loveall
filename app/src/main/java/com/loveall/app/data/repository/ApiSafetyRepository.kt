package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.SafetyApi
import com.loveall.app.data.remote.dto.BlockRequest
import com.loveall.app.data.remote.dto.BlockedUserDto
import com.loveall.app.data.remote.dto.ReportRequest
import com.loveall.app.data.remote.dto.SafetyTipDto
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.domain.repository.SafetyRepository

class ApiSafetyRepository(private val api: SafetyApi) : SafetyRepository {
    override suspend fun tips() = apiCall { api.tips().tips.map(SafetyTipDto::toDomain) }

    override suspend fun blocked() = apiCall { api.blocked().blocked.map(BlockedUserDto::toDomain) }

    override suspend fun block(id: String) = apiCall { api.block(BlockRequest(id)) }

    override suspend fun unblock(id: String) = apiCall { api.unblock(id) }

    override suspend fun report(id: String, reason: ReportReason, description: String) = apiCall {
        api.report(ReportRequest(id, reason.wire, description))
    }
}
