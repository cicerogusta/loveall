package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields

data class BillingConfigurationDto(
    val enabled: Boolean,
    @SerializedName(ApiFields.PRODUCT_IDS) val productIds: List<String>?,
    @SerializedName(ApiFields.OBFUSCATED_ACCOUNT_ID) val accountId: String?,
)
