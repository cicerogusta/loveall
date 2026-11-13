package com.loveall.app.domain.usecase

import com.loveall.app.domain.model.Message
import com.loveall.app.domain.model.MessageTimestamp
import java.time.Instant

/** Combina páginas e atualizações de uma conversa de forma determinística. */
object MessageTimeline {
    /**
     * Agrupa pelo identificador e usa os dados recebidos para atualizar uma mensagem existente. Uma
     * confirmação de leitura já verdadeira nunca volta a falsa por causa de uma resposta antiga.
     * Ordena por instante e por identificador em caso de empate; datas inválidas usam
     * Instant.EPOCH.
     */
    fun merge(current: List<Message>, incoming: List<Message>): List<Message> {
        val byId = current.associateByTo(linkedMapOf(), Message::id)
        incoming.forEach { message ->
            byId[message.id] = message.copy(read = message.read || byId[message.id]?.read == true)
        }
        return byId.values.sortedWith(
            compareBy<Message> { MessageTimestamp.parse(it.createdAt) ?: Instant.EPOCH }
                .thenBy(Message::id)
        )
    }
}
