package com.loveall.app.ui.components.plans

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.loveall.app.domain.model.Checkout

@Composable
internal fun CheckoutBrowserEffect(
    checkout: Checkout?,
    onCheckoutOpened: () -> Unit,
    onUnavailable: () -> Unit,
) {
    val context = LocalContext.current
    val latestOpened by rememberUpdatedState(onCheckoutOpened)
    val latestUnavailable by rememberUpdatedState(onUnavailable)
    LaunchedEffect(checkout) {
        if (checkout == null) return@LaunchedEffect
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkout.url)))
        } catch (_: ActivityNotFoundException) {
            latestUnavailable()
        } catch (_: SecurityException) {
            latestUnavailable()
        }
        latestOpened()
    }
}
