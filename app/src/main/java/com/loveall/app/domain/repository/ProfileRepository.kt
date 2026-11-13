package com.loveall.app.domain.repository

import com.loveall.app.domain.model.Profile
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User

/**
 * Define as operações de perfil necessárias ao aplicativo, independentemente de como são
 * executadas. A implementação HTTP fica em data; testes podem fornecer um fake sem rede ou Android
 * SDK.
 */
interface ProfileRepository {
    suspend fun uploadPhoto(jpeg: ByteArray): String

    suspend fun current(): User

    suspend fun update(edit: ProfileEdit): User

    suspend fun discover(): List<Profile>

    suspend fun profile(id: String): Profile
}
