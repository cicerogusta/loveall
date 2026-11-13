package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.*
import com.loveall.app.data.remote.dto.MessageRequest
import com.loveall.app.data.remote.dto.MessageResponse
import com.loveall.app.data.remote.dto.MessagesResponse
import com.loveall.app.domain.validation.Rules
import retrofit2.http.*

interface MessagesApi {
    @GET(ApiPaths.MESSAGES_BY_MATCH)
    suspend fun messages(
        @Path(ApiParameters.ID) id: String,
        @Query(ApiParameters.PAGE) page: Int,
        @Query(ApiParameters.LIMIT) limit: Int = Rules.MESSAGE_PAGE_SIZE,
    ): MessagesResponse

    @POST(ApiPaths.MESSAGES_BY_MATCH)
    suspend fun send(
        @Path(ApiParameters.ID) id: String,
        @Body body: MessageRequest,
    ): MessageResponse
}
