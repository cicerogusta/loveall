package com.loveall.app.domain.model

/**
 * Representa os valores de status conhecidos do backend e informa se encerram a transação. O valor
 * wire é parte do protocolo; UNKNOWN preserva a possibilidade de consultar estados novos.
 */
enum class PaymentState(val wire: String, val isTerminal: Boolean = false) {
    PENDING("pending"),
    IN_PROCESS("in_process"),
    APPROVED("approved", true),
    REJECTED("rejected", true),
    FAILED("failed", true),
    CANCELLED("cancelled", true),
    REFUNDED("refunded", true),
    CHARGED_BACK("charged_back", true),
    NOT_FOUND("not_found", true),
    UNKNOWN("unknown");

    companion object {
        fun fromWire(value: String?): PaymentState =
            entries.firstOrNull { it.wire == value } ?: UNKNOWN
    }
}
