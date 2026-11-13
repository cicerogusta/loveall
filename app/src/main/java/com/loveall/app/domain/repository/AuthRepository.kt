package com.loveall.app.domain.repository

import com.loveall.app.domain.model.Registration
import com.loveall.app.domain.model.User

/**
 * Contrato de autenticação e encerramento de sessão. A implementação decide como comunicar com a
 * API e persistir o token, sem expor esses detalhes à UI.
 */
interface AuthRepository {
    suspend fun restoreSession(): User?

    suspend fun login(email: String, password: String): User

    suspend fun register(form: Registration): User

    suspend fun logout()

    suspend fun acceptTerms(): User

    suspend fun deleteAccount(password: String)
}
