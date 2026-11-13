package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields

data class VerifyPurchaseRequest(
    @SerializedName(ApiFields.PRODUCT_ID) val productId: String,
    @SerializedName(ApiFields.PURCHASE_TOKEN) val purchaseToken: String,
)
