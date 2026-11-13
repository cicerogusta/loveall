package com.loveall.app.domain.model

/**
 * Mensagem confirmada pela API. createdAt preserva o timestamp do transporte; MessageTimestamp
 * interpreta esse valor para ordenar e exibir. read representa a confirmação de leitura
 * interpretada do campo recebido pelo DTO.
 */
data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val read: Boolean,
)
