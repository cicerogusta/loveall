package com.loveall.app

import android.app.Activity
import android.content.Context
import com.loveall.app.data.*
import com.loveall.app.data.local.KeystoreSessionStore
import com.loveall.app.data.remote.api.AuthApi
import com.loveall.app.data.remote.api.BillingApi
import com.loveall.app.data.remote.api.ConnectionsApi
import com.loveall.app.data.remote.api.MessagesApi
import com.loveall.app.data.remote.api.PlansApi
import com.loveall.app.data.remote.api.ProfileApi
import com.loveall.app.data.remote.api.SafetyApi
import com.loveall.app.data.repository.ApiBillingRepository
import com.loveall.app.data.repository.ApiConnectionsRepository
import com.loveall.app.data.repository.ApiMessagesRepository
import com.loveall.app.data.repository.ApiPlansRepository
import com.loveall.app.data.repository.ApiProfileRepository
import com.loveall.app.data.repository.ApiSafetyRepository
import com.loveall.app.data.repository.DefaultAuthRepository
import com.loveall.app.domain.repository.AuthRepository
import com.loveall.app.domain.repository.BillingRepository
import com.loveall.app.domain.repository.ConnectionsRepository
import com.loveall.app.domain.repository.MessagesRepository
import com.loveall.app.domain.repository.PlansRepository
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.domain.repository.SafetyRepository
import com.loveall.app.domain.repository.SubscriptionStore
import com.loveall.app.domain.usecase.AddProfilePhoto
import com.loveall.app.platform.ads.AdConsentController
import com.loveall.app.platform.billing.GoogleSubscriptionStore
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Ponto de composição das dependências do aplicativo.
 *
 * Cria implementações concretas e as expõe pelos contratos do domínio. Os ViewModels recebem esses
 * contratos por construtor; nos testes, eles podem ser substituídos por fakes. O cliente
 * autenticado acrescenta o token, enquanto o cliente básico atende a autenticação.
 *
 * @param apiBaseUrl endereço usado pelo Retrofit e pela resolução das fotos; pode ser injetado nos
 *   testes.
 */
class AppContainer(context: Context, val apiBaseUrl: String = BuildConfig.API_BASE_URL) {
    private val mutableExpiredSessions = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expiredSessions = mutableExpiredSessions.asSharedFlow()
    private val sessionStore = KeystoreSessionStore(context, Dispatchers.IO)
    private val client =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()

    private fun retrofit(client: OkHttpClient) =
        Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val authRepository: AuthRepository =
        DefaultAuthRepository(retrofit(client).create(AuthApi::class.java), sessionStore)
    // O cliente autenticado deriva do básico, mas acrescenta o interceptor apenas onde ele é
    // necessário.
    private val authenticatedClient =
        client
            .newBuilder()
            .addInterceptor(
                SessionAuthInterceptor(sessionStore) {
                    mutableExpiredSessions.tryEmit(Unit)
                }
            )
            .build()
    private val authenticatedRetrofit = retrofit(authenticatedClient)
    val profiles: ProfileRepository =
        ApiProfileRepository(authenticatedRetrofit.create(ProfileApi::class.java))
    val connections: ConnectionsRepository =
        ApiConnectionsRepository(authenticatedRetrofit.create(ConnectionsApi::class.java))
    val messages: MessagesRepository =
        ApiMessagesRepository(authenticatedRetrofit.create(MessagesApi::class.java))
    val safety: SafetyRepository =
        ApiSafetyRepository(authenticatedRetrofit.create(SafetyApi::class.java))
    val plans: PlansRepository =
        ApiPlansRepository(
            authenticatedRetrofit.create(PlansApi::class.java),
            BuildConfig.EXTERNAL_CHECKOUT_ENABLED,
        )
    val billing: BillingRepository =
        ApiBillingRepository(authenticatedRetrofit.create(BillingApi::class.java))
    private val playStore = GoogleSubscriptionStore(context.applicationContext)
    val subscriptionStore: SubscriptionStore = playStore
    val adConsent =
        AdConsentController(
            context.applicationContext,
            BuildConfig.ADS_ENABLED,
            BuildConfig.ADMOB_APP_ID,
        )

    fun attachActivity(activity: Activity) = playStore.attach(activity)

    fun detachActivity(activity: Activity) = playStore.detach(activity)

    val addProfilePhoto =
        AddProfilePhoto(
            ProfilePhotoReader(context.applicationContext.contentResolver, Dispatchers.IO),
            profiles,
        )

    private companion object {
        const val CONNECT_TIMEOUT_SECONDS = 15L
        // A instância gratuita pode levar cerca de um minuto para despertar.
        const val READ_TIMEOUT_SECONDS = 90L
        const val CALL_TIMEOUT_SECONDS = 120L
    }
}
