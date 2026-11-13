package com.loveall.app.data.remote.dto

import retrofit2.http.*

data class PlanDto(val name: String, val price: Double, val minutes: Int)
