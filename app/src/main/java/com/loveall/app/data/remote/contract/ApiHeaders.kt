package com.loveall.app.data.remote.contract

object ApiHeaders {
    const val AUTHORIZATION = "Authorization"
    private const val BEARER_PREFIX = "Bearer "

    fun bearer(token: String): String = BEARER_PREFIX + token
}
