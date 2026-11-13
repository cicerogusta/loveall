package com.loveall.app.data

import java.io.IOException

internal class SessionStorageIOException(cause: Exception) :
    IOException(STORAGE_ERROR_MESSAGE, cause)

private const val STORAGE_ERROR_MESSAGE = "Secure session storage is unavailable"
