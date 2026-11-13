package com.loveall.app.domain.error

enum class AuthError {
    REQUIRED_CREDENTIALS,
    INVALID_CREDENTIALS,
    SESSION_EXPIRED,
    NETWORK,
    SERVICE,
    STORAGE,
    VALIDATION,
    FORBIDDEN,
    RATE_LIMIT,
    NOT_FOUND,
}
