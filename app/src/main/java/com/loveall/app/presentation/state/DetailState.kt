package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Profile

/**
 * Estado do perfil público e das ações de segurança sobre aquela pessoa. blocked sinaliza que a UI
 * deve voltar após o sucesso; reported permite apresentar a confirmação. As flags blocking e
 * reporting impedem ações de escrita sobrepostas.
 */
data class DetailState(
    val profile: Profile? = null,
    val blocked: Boolean = false,
    val reported: Boolean = false,
    val loading: Boolean = false,
    val blocking: Boolean = false,
    val reporting: Boolean = false,
    val error: AuthError? = null,
) {
    val busy: Boolean
        get() = blocking || reporting
}
