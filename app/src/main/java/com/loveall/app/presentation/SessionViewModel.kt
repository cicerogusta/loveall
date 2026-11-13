package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Registration
import com.loveall.app.domain.model.User
import com.loveall.app.domain.repository.AuthRepository
import com.loveall.app.presentation.state.SessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Controla o acesso à navegação pública ou autenticada a partir de SessionState. Restauração,
 * login, cadastro e logout usam o repositório de autenticação. Uma expiração pode cancelar a
 * operação atual para impedir autenticação por uma resposta atrasada.
 */
class SessionViewModel(private val auth: AuthRepository, expired: Flow<Unit>) : ViewModel() {
    private val mutableState = MutableStateFlow(SessionState())
    private var operationJob: Job? = null
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch { expired.collect { logout(expired = true) } }
        restore()
    }

    /**
     * Tenta recuperar a conta do token persistido. Uma falha temporária permite nova tentativa; a
     * ausência de token leva ao fluxo público sem simular uma conta autenticada.
     */
    fun restore() {
        if (state.value.busy) return
        mutableState.update {
            it.copy(restoring = true, restoreFailed = false, busy = true, error = null)
        }
        operationJob = viewModelScope.launch {
            try {
                val user = auth.restoreSession()
                currentCoroutineContext().ensureActive()
                mutableState.value = SessionState(restoring = false, user = user)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val failure = error.toUiError()
                mutableState.value =
                    SessionState(
                        restoring = false,
                        restoreFailed = failure != AuthError.SESSION_EXPIRED,
                        error = failure,
                    )
            }
        }
    }

    fun login(email: String, password: String) = authenticate { auth.login(email, password) }

    fun register(form: Registration) = authenticate(newAccount = true) { auth.register(form) }

    /**
     * Executa login ou cadastro e publica a conta somente depois de concluir a operação. A
     * verificação de cancelamento impede publicar um resultado de uma operação já invalidada.
     */
    private fun authenticate(newAccount: Boolean = false, operation: suspend () -> User) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        operationJob = viewModelScope.launch {
            try {
                val user = operation()
                currentCoroutineContext().ensureActive()
                mutableState.value =
                    SessionState(restoring = false, user = user, newAccount = newAccount)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(busy = false, error = error.toUiError()) }
            }
        }
    }

    /**
     * Remove a sessão persistida e publica o estado público. Quando chamado por expiração, cancela
     * uma autenticação em curso mesmo que busy esteja ativo.
     */
    fun logout(expired: Boolean = false) {
        if (state.value.busy && !expired) return
        // An expired session must invalidate in-flight authentication rather than be dropped as
        // "busy".
        operationJob?.cancel()
        mutableState.update { it.copy(busy = true) }
        operationJob = viewModelScope.launch {
            try {
                auth.logout()
                mutableState.value =
                    SessionState(
                        restoring = false,
                        error = if (expired) AuthError.SESSION_EXPIRED else null,
                    )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value =
                    SessionState(restoring = false, restoreFailed = true, error = error.toUiError())
            }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }

    fun acceptTerms() = authenticate { auth.acceptTerms() }

    fun deleteAccount(password: String) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        operationJob = viewModelScope.launch {
            try {
                auth.deleteAccount(password)
                currentCoroutineContext().ensureActive()
                mutableState.value = SessionState(restoring = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(busy = false, error = error.toUiError()) }
            }
        }
    }

    fun updateUser(user: User) {
        mutableState.update { it.copy(user = user, newAccount = false) }
    }
}
