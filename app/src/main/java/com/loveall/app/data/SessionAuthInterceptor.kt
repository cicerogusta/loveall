package com.loveall.app.data

import com.loveall.app.data.local.SessionStore
import com.loveall.app.data.remote.contract.ApiHeaders
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Acrescenta autorização às requisições e comunica a expiração da sessão. O interceptor é síncrono:
 * a leitura suspensa do token é aguardada no fluxo da chamada OkHttp, não na montagem das telas. O
 * callback apenas emite um sinal; a navegação é decidida pela UI.
 */
class SessionAuthInterceptor(private val store: SessionStore, private val onExpired: () -> Unit) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = readToken()
        val request =
            chain
                .request()
                .newBuilder()
                .apply {
                    if (token != null) header(ApiHeaders.AUTHORIZATION, ApiHeaders.bearer(token))
                }
                .build()
        val response = chain.proceed(request)
        try {
            // Uma resposta antiga não deve encerrar a sessão de uma conta que entrou depois.
            if (response.code == 401 && readToken() == token) onExpired()
            return response
        } catch (error: IOException) {
            response.close()
            throw error
        }
    }

    private fun readToken(): String? =
        try {
            runBlocking { store.read() }
        } catch (error: Exception) {
            // Falhas do interceptor precisam chegar ao OkHttp como I/O para o tratamento normal da
            // chamada.
            throw SessionStorageIOException(error)
        }
}
