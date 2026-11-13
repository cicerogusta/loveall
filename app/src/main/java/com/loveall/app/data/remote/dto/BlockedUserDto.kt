package com.loveall.app.data.remote.dto

import com.loveall.app.domain.model.BlockedUser
import retrofit2.http.*

data class BlockedUserDto(val id: String, val name: String) {
    fun toDomain() = BlockedUser(id, name)
}
