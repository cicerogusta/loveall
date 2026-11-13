package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User

/**
 * Estado do editor e da visualização da própria conta.
 *
 * @property user versão confirmada pela API, distinta dos campos que ainda estão em edição.
 * @property edit rascunho local; não existe antes da primeira carga bem-sucedida.
 * @property saved indica que o rascunho foi confirmado; uma nova edição remove esse sinal.
 * @property loading consulta do perfil em andamento.
 * @property saving envio do rascunho em andamento.
 * @property uploadingPhoto preparação e envio de uma foto em andamento.
 * @property error categoria de falha apresentada pela UI.
 */
data class ProfileState(
    val user: User? = null,
    val edit: ProfileEdit? = null,
    val saved: Boolean = false,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val uploadingPhoto: Boolean = false,
    val error: AuthError? = null,
) {
    val busy: Boolean
        get() = saving || uploadingPhoto
}
