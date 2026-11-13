package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class SubscriptionDto(
    val plan: String,
    @SerializedName(ApiFields.CALL_MINUTES_REMAINING) val minutes: Int,
    @SerializedName(ApiFields.PAYMENT_STATUS) val paymentStatus: String?,
)
