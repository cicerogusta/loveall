package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class CallRequest(
    @SerializedName(ApiFields.RECEIVER_ID) val receiverId: String,
    @SerializedName(ApiFields.CALL_TYPE) val type: String,
)
