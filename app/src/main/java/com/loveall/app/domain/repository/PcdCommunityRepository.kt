package com.loveall.app.domain.repository

import com.loveall.app.domain.model.Profile

/** Participação é autodeclarada e exige consentimento separado do cadastro. */
interface PcdCommunityRepository {
    suspend fun isMember(): Boolean

    suspend fun setMembership(enabled: Boolean, consent: Boolean): Boolean

    suspend fun discover(): List<Profile>

    suspend fun like(id: String): String?
}
