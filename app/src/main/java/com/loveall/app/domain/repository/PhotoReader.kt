package com.loveall.app.domain.repository

/** Prepares a selected image without exposing Android types to presentation or domain logic. */
fun interface PhotoReader {
    suspend fun read(reference: String): ByteArray
}
