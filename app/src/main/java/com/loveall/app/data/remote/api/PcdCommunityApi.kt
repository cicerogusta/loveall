package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.ApiPaths
import com.loveall.app.data.remote.dto.*
import retrofit2.http.*

interface PcdCommunityApi {
    @GET(ApiPaths.PCD_MEMBERSHIP) suspend fun membership(): PcdMembership

    @PUT(ApiPaths.PCD_MEMBERSHIP) suspend fun membership(@Body body: PcdMembership): PcdMembership

    @GET(ApiPaths.PCD_DISCOVER) suspend fun discover(): ProfilesResponse

    @POST(ApiPaths.PCD_LIKE) suspend fun like(@Body body: LikeRequest): LikeResult
}
