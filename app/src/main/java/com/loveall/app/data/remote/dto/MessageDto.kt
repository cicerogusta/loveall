package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import com.loveall.app.domain.model.Message
import retrofit2.http.*

data class MessageDto(
    val id: String,
    @SerializedName(ApiFields.SENDER_ID) val senderId: String,
    val content: String,
    @SerializedName(ApiFields.CREATED_AT) val createdAt: String,
    @SerializedName(ApiFields.IS_READ) val read: Int,
) {
    fun toDomain() = Message(id, senderId, content, createdAt, read == 1)
}
