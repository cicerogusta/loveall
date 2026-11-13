package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.BlockedUser
import com.loveall.app.domain.model.SafetyTip

/**
 * Orientações e lista de bloqueios carregadas pela API. unblockingId identifica a pessoa cujo
 * desbloqueio está em andamento, antes da remoção da lista.
 */
data class SafetyState(
    val tips: List<SafetyTip> = emptyList(),
    val blocked: List<BlockedUser> = emptyList(),
    val loading: Boolean = false,
    val unblockingId: String? = null,
    val error: AuthError? = null,
) {
    val busy: Boolean
        get() = unblockingId != null
}
