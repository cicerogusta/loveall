package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class ReportRequest(
    @SerializedName(ApiFields.REPORTED_ID) val reportedId: String,
    val reason: String,
    val description: String,
)
