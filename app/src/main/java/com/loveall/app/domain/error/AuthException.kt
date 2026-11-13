package com.loveall.app.domain.error

class AuthException(val error: AuthError) : Exception()
