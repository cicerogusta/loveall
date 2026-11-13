package com.loveall.app.data.local

/**
 * Contrato mínimo de persistência do token: leitura, escrita e remoção. A cifragem pertence à
 * implementação Android; o fake de teste utiliza memória. Não armazena senhas.
 */
interface SessionStore {
    suspend fun read(): String?

    suspend fun write(token: String)

    suspend fun clear()
}
