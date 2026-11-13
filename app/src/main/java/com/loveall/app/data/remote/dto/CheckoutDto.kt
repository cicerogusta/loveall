package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class CheckoutDto(
    @SerializedName(ApiFields.TRANSACTION_ID) val transactionId: String,
    @SerializedName(ApiFields.PAYMENT_URL) val paymentUrl: String,
)
