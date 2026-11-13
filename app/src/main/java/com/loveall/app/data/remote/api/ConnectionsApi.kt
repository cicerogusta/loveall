package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.*
import com.loveall.app.data.remote.dto.LikeRequest
import com.loveall.app.data.remote.dto.LikeResult
import com.loveall.app.data.remote.dto.LikesResponse
import com.loveall.app.data.remote.dto.MatchesResponse
import retrofit2.http.*

interface ConnectionsApi {
    @POST(ApiPaths.LIKE) suspend fun like(@Body body: LikeRequest): LikeResult

    @POST(ApiPaths.PASS) suspend fun pass(@Body body: LikeRequest)

    @GET(ApiPaths.INCOMING_LIKES) suspend fun likes(): LikesResponse

    @GET(ApiPaths.MATCHES) suspend fun matches(): MatchesResponse

    @DELETE(ApiPaths.MATCH_BY_ID) suspend fun unmatch(@Path(ApiParameters.ID) id: String)
}
