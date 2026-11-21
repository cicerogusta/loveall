package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.PcdCommunityApi
import com.loveall.app.data.remote.dto.LikeRequest
import com.loveall.app.data.remote.dto.PcdMembership
import com.loveall.app.data.remote.dto.ProfileDto
import com.loveall.app.domain.repository.PcdCommunityRepository

class ApiPcdCommunityRepository(private val api: PcdCommunityApi) : PcdCommunityRepository {
    override suspend fun isMember() = apiCall { api.membership().enabled }

    override suspend fun setMembership(enabled: Boolean, consent: Boolean) = apiCall {
        api.membership(PcdMembership(enabled, consent)).enabled
    }

    override suspend fun discover() = apiCall { api.discover().profiles.map(ProfileDto::toDomain) }

    override suspend fun like(id: String) = apiCall { api.like(LikeRequest(id)).match?.id }
}
