package com.loveall.app.data.remote.dto

import com.loveall.app.domain.model.SafetyTip
import retrofit2.http.*

data class SafetyTipDto(val title: String, val text: String) {
    fun toDomain() = SafetyTip(title, text)
}
