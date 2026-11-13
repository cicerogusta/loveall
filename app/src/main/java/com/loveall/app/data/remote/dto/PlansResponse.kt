package com.loveall.app.data.remote.dto

import retrofit2.http.*

data class PlansResponse(val plans: Map<String, PlanDto>)
