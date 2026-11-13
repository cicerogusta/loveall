package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class CallResult(
    val allowed: Boolean,
    @SerializedName(ApiFields.MINUTES_REMAINING) val minutes: Int,
)
