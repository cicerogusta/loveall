package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.User

/**
 * Determina qual parte do aplicativo pode ser apresentada.
 *
 * @property restoring mantém a tela de carregamento durante a restauração inicial.
 * @property restoreFailed diferencia falha temporária de ausência de conta, permitindo tentar
 *   novamente.
 * @property user conta autenticada; nula quando a navegação pública deve ser usada, após a
 *   restauração.
 * @property busy operação de sessão em andamento, independente das operações de perfil ou chat.
 * @property newAccount orienta a primeira navegação de uma conta recém-cadastrada para o editor.
 */
data class SessionState(
    val restoring: Boolean = true,
    val restoreFailed: Boolean = false,
    val user: User? = null,
    val busy: Boolean = false,
    val error: AuthError? = null,
    val newAccount: Boolean = false,
)
