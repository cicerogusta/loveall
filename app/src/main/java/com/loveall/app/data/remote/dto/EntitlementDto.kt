package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields

data class EntitlementDto(
    @SerializedName(ApiFields.AD_FREE) val adFree: Boolean?,
    @SerializedName(ApiFields.EXPIRES_AT) val expiresAt: String?,
)
