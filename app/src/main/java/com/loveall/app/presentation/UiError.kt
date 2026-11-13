package com.loveall.app.presentation

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException

internal fun Exception.toUiError(): AuthError = (this as? AuthException)?.error ?: AuthError.SERVICE
