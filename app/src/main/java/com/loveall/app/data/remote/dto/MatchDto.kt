package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class MatchDto(
    @SerializedName(ApiFields.MATCH_ID) val matchId: String,
    val partner: ProfileDto?,
    @SerializedName(ApiFields.LAST_MESSAGE) val lastMessage: LastMessageDto?,
    @SerializedName(ApiFields.UNREAD_COUNT) val unread: Int,
)
