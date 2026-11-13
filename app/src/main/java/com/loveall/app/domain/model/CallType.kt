package com.loveall.app.domain.model

/**
 * Limita os tipos de chamada aceitos pelos callbacks e contratos internos. wire mantém o valor
 * textual que o DTO envia ao backend.
 */
enum class CallType(val wire: String) {
    AUDIO("audio"),
    VIDEO("video"),
}
