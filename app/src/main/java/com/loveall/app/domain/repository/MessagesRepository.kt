package com.loveall.app.domain.repository

import com.loveall.app.domain.model.Message

/**
 * Contrato de consulta paginada e envio de mensagens. O envio devolve a mensagem confirmada, que
 * será acrescentada ao estado da conversa.
 */
interface MessagesRepository {
    suspend fun messages(matchId: String, page: Int): List<Message>

    suspend fun send(matchId: String, content: String): Message
}
