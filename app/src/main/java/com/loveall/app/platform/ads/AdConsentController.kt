package com.loveall.app.platform.ads

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.ump.*
import com.loveall.app.domain.validation.AdConsentGate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Nunca inicializa o SDK de anúncios antes de UMP permitir requisições. */
class AdConsentController(
    context: Context,
    private val enabled: Boolean,
    private val appId: String,
) {
    private val appContext = context.applicationContext
    private val information = UserMessagingPlatform.getConsentInformation(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(AdConsentState())
    val state = mutableState.asStateFlow()
    private var gathering = false
    private var initialized = false
    private var initializing = false
    private val gate = AdConsentGate()

    fun gather(activity: Activity) {
        if (!enabled || gathering || activity.isFinishing || activity.isDestroyed) return
        gathering = true
        gate.beginUpdate()
        mutableState.update { it.copy(ready = false) }
        information.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                if (activity.isFinishing || activity.isDestroyed) {
                    gathering = false
                    return@requestConsentInfoUpdate
                }
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    gathering = false
                    if (error == null) {
                        gate.confirm(information.canRequestAds())
                        updatePermission()
                    } else denyAfterFailure()
                }
            },
            {
                gathering = false
                // Na falha não fazemos uma nova requisição de anúncio com consentimento
                // desconhecido.
                denyAfterFailure()
            },
        )
    }

    fun showPrivacyOptions(activity: Activity) {
        if (!enabled || activity.isFinishing || activity.isDestroyed) return
        mutableState.update { it.copy(ready = false) }
        gate.beginUpdate()
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error == null) {
                gate.confirm(information.canRequestAds())
                updatePermission()
            } else denyAfterFailure()
        }
    }

    private fun denyAfterFailure() {
        gate.fail()
        mutableState.value =
            AdConsentState(
                false,
                information.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
                true,
            )
    }

    private fun updatePermission() {
        val allowed = gate.canInitialize && information.canRequestAds()
        val required =
            information.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        mutableState.value = AdConsentState(allowed && gate.canLoadAds, required)
        if (!allowed || initialized || initializing) return
        initializing = true
        scope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(appContext, InitializationConfig.Builder(appId).build()) {
                    scope.launch {
                        initialized = true
                        gate.sdkInitialized()
                        initializing = false
                        updatePermission()
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main.immediate) {
                    initializing = false
                    mutableState.value = AdConsentState(false, required)
                }
            }
        }
    }
}
