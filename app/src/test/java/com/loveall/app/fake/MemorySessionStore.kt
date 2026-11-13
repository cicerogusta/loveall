package com.loveall.app.fake

import com.loveall.app.data.local.SessionStore

class MemorySessionStore(var token: String? = null) : SessionStore {
    var failWrite = false

    override suspend fun read() = token

    override suspend fun write(token: String) {
        if (failWrite) error(STORAGE_UNAVAILABLE_MESSAGE)
        this.token = token
    }

    override suspend fun clear() {
        token = null
    }
}

private const val STORAGE_UNAVAILABLE_MESSAGE = "Storage unavailable"
