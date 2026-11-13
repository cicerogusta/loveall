package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.ConnectionsApi
import com.loveall.app.data.remote.dto.LikeRequest
import com.loveall.app.data.remote.dto.ProfileDto
import com.loveall.app.domain.model.Match
import com.loveall.app.domain.repository.ConnectionsRepository

class ApiConnectionsRepository(private val api: ConnectionsApi) : ConnectionsRepository {
    override suspend fun like(id: String, superLike: Boolean) = apiCall {
        api.like(LikeRequest(id, superLike)).match?.id
    }

    override suspend fun pass(id: String) = apiCall { api.pass(LikeRequest(id)) }

    override suspend fun likes() = apiCall { api.likes().likes.map(ProfileDto::toDomain) }

    override suspend fun matches() = apiCall {
        api.matches().matches.mapNotNull { match ->
            match.partner?.let {
                Match(match.matchId, it.toDomain(), match.lastMessage?.content, match.unread)
            }
        }
    }

    override suspend fun unmatch(id: String) = apiCall { api.unmatch(id) }
}
