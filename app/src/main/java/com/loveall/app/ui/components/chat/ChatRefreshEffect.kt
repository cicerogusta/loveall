package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.loveall.app.domain.validation.Rules
import kotlinx.coroutines.delay

/**
 * Executa consultas periódicas somente enquanto a tela está STARTED. rememberUpdatedState mantém o
 * callback recente sem reiniciar o intervalo a cada recomposição.
 */
@Composable
internal fun ChatRefreshEffect(onRefresh: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestRefresh by rememberUpdatedState(onRefresh)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(Rules.CHAT_REFRESH_MILLIS)
                latestRefresh()
            }
        }
    }
}
