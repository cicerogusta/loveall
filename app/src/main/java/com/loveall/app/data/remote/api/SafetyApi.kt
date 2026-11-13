package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.*
import com.loveall.app.data.remote.dto.BlockRequest
import com.loveall.app.data.remote.dto.BlockedResponse
import com.loveall.app.data.remote.dto.ReportRequest
import com.loveall.app.data.remote.dto.TipsResponse
import retrofit2.http.*

interface SafetyApi {
    @GET(ApiPaths.SAFETY_TIPS) suspend fun tips(): TipsResponse

    @GET(ApiPaths.BLOCKED_USERS) suspend fun blocked(): BlockedResponse

    @POST(ApiPaths.BLOCK_USER) suspend fun block(@Body body: BlockRequest)

    @DELETE(ApiPaths.UNBLOCK_USER) suspend fun unblock(@Path(ApiParameters.ID) id: String)

    @POST(ApiPaths.REPORT_USER) suspend fun report(@Body body: ReportRequest)
}
