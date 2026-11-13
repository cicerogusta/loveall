package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Profile

/**
 * Estado da fila de descoberta ou de curtidas recebidas.
 *
 * @property profiles candidatos disponíveis; o primeiro é o cartão apresentado pela tela.
 * @property matchId identificador de uma conexão confirmada, usado pelo diálogo para abrir o chat.
 * @property matchedName nome exibido no diálogo da conexão confirmada.
 * @property processingProfileId candidato cuja ação aguarda confirmação do servidor.
 */
data class ExploreState(
    val profiles: List<Profile> = emptyList(),
    val matchId: String? = null,
    val matchedName: String? = null,
    val loading: Boolean = false,
    val processingProfileId: String? = null,
    val error: AuthError? = null,
) {
    val busy: Boolean
        get() = processingProfileId != null
}
