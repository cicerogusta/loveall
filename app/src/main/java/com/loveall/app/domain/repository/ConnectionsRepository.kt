package com.loveall.app.domain.repository

import com.loveall.app.domain.model.Match
import com.loveall.app.domain.model.Profile

/**
 * Contrato de curtidas, decisões de passar, matches e encerramento de conexões. O retorno de like
 * informa se houve uma conexão confirmada para abrir a conversa.
 */
interface ConnectionsRepository {
    suspend fun like(id: String, superLike: Boolean = false): String?

    suspend fun pass(id: String)

    suspend fun matches(): List<Match>

    suspend fun likes(): List<Profile>

    suspend fun unmatch(id: String)
}
