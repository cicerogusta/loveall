package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class PaymentStatusDto(
    @SerializedName(ApiFields.PAYMENT_STATUS) val paymentStatus: String?,
    val status: String?,
    val plan: String?,
)
