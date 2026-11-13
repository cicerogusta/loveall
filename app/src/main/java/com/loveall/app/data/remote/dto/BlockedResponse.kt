package com.loveall.app.data.remote.dto

import retrofit2.http.*

data class BlockedResponse(val blocked: List<BlockedUserDto>)
