package com.loveall.app.data

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/**
 * Fronteira de conversão entre exceções de transporte e erros entendidos pelo aplicativo. O mesmo
 * HTTP 401 representa credencial inválida durante login e sessão expirada nas demais consultas.
 * Cancelamento é propagado para que sair de um fluxo não apareça como erro de conexão.
 */
internal suspend fun <T> apiCall(login: Boolean = false, block: suspend () -> T): T =
    try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: AuthException) {
        throw error
    } catch (error: HttpException) {
        throw AuthException(
            when (error.code()) {
                400,
                409,
                422 -> AuthError.VALIDATION
                401 -> if (login) AuthError.INVALID_CREDENTIALS else AuthError.SESSION_EXPIRED
                403 -> AuthError.FORBIDDEN
                404 -> AuthError.NOT_FOUND
                429 -> AuthError.RATE_LIMIT
                else -> AuthError.SERVICE
            }
        )
    } catch (error: SessionStorageIOException) {
        throw AuthException(AuthError.STORAGE)
    } catch (error: IOException) {
        throw AuthException(AuthError.NETWORK)
    } catch (error: Exception) {
        throw AuthException(AuthError.SERVICE)
    }
