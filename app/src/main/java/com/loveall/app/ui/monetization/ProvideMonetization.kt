package com.loveall.app.ui.monetization

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.loveall.app.AppContainer
import com.loveall.app.BuildConfig
import com.loveall.app.presentation.MonetizationViewModel
import com.loveall.app.ui.navigation.screenViewModel
import kotlinx.coroutines.delay

@Composable
fun ProvideMonetization(container: AppContainer, userId: String?, content: @Composable () -> Unit) {
    val vm = screenViewModel {
        MonetizationViewModel(
            container.billing,
            container.subscriptionStore,
            BuildConfig.PLAY_BILLING_ENABLED,
            BuildConfig.ADS_ENABLED,
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(userId) { vm.bindAccount(userId) }
    LaunchedEffect(userId, lifecycle) {
        if (userId != null && (BuildConfig.ADS_ENABLED || BuildConfig.PLAY_BILLING_ENABLED)) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    vm.refresh()
                    delay(REFRESH_INTERVAL_MS)
                }
            }
        }
    }
    CompositionLocalProvider(
        LocalMonetization provides MonetizationUi(state, vm::refresh, vm::restore, vm::purchase),
        LocalAdConsent provides container.adConsent,
        content = content,
    )
}

private const val REFRESH_INTERVAL_MS = 300_000L
