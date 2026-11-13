package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.local.SessionStore
import com.loveall.app.data.remote.api.AuthApi
import com.loveall.app.data.remote.contract.ApiHeaders
import com.loveall.app.data.remote.dto.AcceptTermsRequest
import com.loveall.app.data.remote.dto.DeleteAccountRequest
import com.loveall.app.data.remote.dto.LoginRequest
import com.loveall.app.data.remote.dto.RegistrationRequest
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.Registration
import com.loveall.app.domain.model.User
import com.loveall.app.domain.repository.AuthRepository
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementa autenticação HTTP e persistência do token. O Mutex serializa operações que leem ou
 * alteram a sessão. O Clock injetado torna a validação de idade previsível nos testes, sem depender
 * da data do computador.
 */
class DefaultAuthRepository(
    private val api: AuthApi,
    private val store: SessionStore,
    private val clock: Clock = Clock.systemDefaultZone(),
) : AuthRepository {
    private val mutex = Mutex()

    /**
     * Retorna null quando não existe token. Se a API rejeitar a sessão, apaga o token; falhas
     * temporárias de rede são propagadas sem apagar uma credencial que pode continuar válida.
     */
    override suspend fun restoreSession(): User? = mutex.withLock {
        val token = storage { store.read() } ?: return@withLock null
        try {
            apiCall { requireNotNull(api.me(ApiHeaders.bearer(token)).user).toDomain() }
        } catch (error: AuthException) {
            if (error.error == AuthError.SESSION_EXPIRED) storage { store.clear() }
            throw error
        }
    }

    /**
     * Valida os campos mínimos, remove espaços externos do email e preserva a senha recebida. Só
     * persiste o token depois de validar os dados essenciais da resposta de sucesso.
     */
    override suspend fun login(email: String, password: String): User = mutex.withLock {
        if (email.isBlank() || password.isBlank())
            throw AuthException(AuthError.REQUIRED_CREDENTIALS)
        val response = apiCall(login = true) { api.login(LoginRequest(email.trim(), password)) }
        val user = apiCall { requireNotNull(response.user).toDomain() }
        val token =
            response.token?.takeIf { it.isNotBlank() } ?: throw AuthException(AuthError.SERVICE)
        storage { store.write(token) }
        user
    }

    override suspend fun logout() = mutex.withLock { storage { store.clear() } }

    override suspend fun acceptTerms(): User = mutex.withLock {
        val token = storage { store.read() } ?: throw AuthException(AuthError.SESSION_EXPIRED)
        apiCall {
            requireNotNull(api.acceptTerms(ApiHeaders.bearer(token), AcceptTermsRequest()).user)
                .toDomain()
        }
    }

    /** A conta só é removida localmente depois da confirmação de exclusão no servidor. */
    override suspend fun deleteAccount(password: String) = mutex.withLock {
        if (password.isBlank()) throw AuthException(AuthError.REQUIRED_CREDENTIALS)
        val token = storage { store.read() } ?: throw AuthException(AuthError.SESSION_EXPIRED)
        apiCall { api.deleteAccount(ApiHeaders.bearer(token), DeleteAccountRequest(password)) }
        storage { store.clear() }
    }

    /**
     * Valida o cadastro na data fornecida pelo Clock, envia o DTO e persiste a sessão confirmada.
     */
    override suspend fun register(form: Registration): User = mutex.withLock {
        if (!form.isValid(LocalDate.now(clock))) throw AuthException(AuthError.VALIDATION)
        val response = apiCall { api.register(RegistrationRequest.from(form)) }
        val user = apiCall { requireNotNull(response.user).toDomain() }
        val token =
            response.token?.takeIf(String::isNotBlank) ?: throw AuthException(AuthError.SERVICE)
        storage { store.write(token) }
        user
    }

    /**
     * Converte falhas do armazenamento seguro para o erro de domínio correspondente. Cancelamentos
     * continuam sendo cancelamentos e não são apresentados como falha de persistência.
     */
    private suspend fun <T> storage(block: suspend () -> T): T =
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AuthException(AuthError.STORAGE)
        }
}
