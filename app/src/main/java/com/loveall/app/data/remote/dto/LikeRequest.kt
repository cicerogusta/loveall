package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import retrofit2.http.*

data class LikeRequest(
    @SerializedName(ApiFields.TO_USER) val toUser: String,
    @SerializedName(ApiFields.IS_SUPER_LIKE) val superLike: Boolean = false,
)
