package com.loveall.app.domain.repository

import com.loveall.app.domain.model.BlockedUser
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.domain.model.SafetyTip

/**
 * Contrato das ações e consultas de segurança. Os ViewModels aguardam a confirmação da
 * implementação antes de sinalizar sucesso à UI.
 */
interface SafetyRepository {
    suspend fun tips(): List<SafetyTip>

    suspend fun blocked(): List<BlockedUser>

    suspend fun block(id: String)

    suspend fun unblock(id: String)

    suspend fun report(id: String, reason: ReportReason, description: String)
}
