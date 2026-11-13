package com.loveall.app.ui.monetization

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.ads.mobile.sdk.banner.*
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.loveall.app.BuildConfig
import com.loveall.app.R
import com.loveall.app.domain.validation.AdEligibility

@Composable
fun AdBanner() {
    if (!BuildConfig.ADS_ENABLED || LocalInspectionMode.current) return
    val state = LocalMonetization.current.state
    if (
        !AdEligibility.canDisplay(
            state.entitlementKnown,
            state.entitlement.active,
            state.pending,
            state.busy,
        )
    )
        return
    val controller = LocalAdConsent.current ?: return
    val consent by controller.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    LaunchedEffect(controller, activity) { controller.gather(activity) }
    if (consent.ready) LoadedBanner(activity)
}

@Composable
private fun LoadedBanner(activity: Activity) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth.value.toInt().coerceAtLeast(MIN_BANNER_WIDTH)
        val view = remember(activity, width) { AdView(activity) }
        val size =
            remember(activity, width) {
                AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, width)
            }
        var loaded by remember(view) { mutableStateOf(false) }
        DisposableEffect(view) { onDispose { view.destroy() } }
        LaunchedEffect(view, size) {
            view.loadAd(
                BannerAdRequest.Builder(BuildConfig.ADMOB_BANNER_ID, size).build(),
                object : AdLoadCallback<BannerAd> {
                    override fun onAdLoaded(ad: BannerAd) {
                        loaded = true
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        loaded = false
                    }
                },
            )
        }
        Column {
            if (loaded)
                Text(
                    stringResource(R.string.advertisement),
                    style = MaterialTheme.typography.labelSmall,
                )
            AndroidView(
                factory = { view },
                modifier = Modifier.fillMaxWidth().height(if (loaded) size.height.dp else 0.dp),
            )
        }
    }
}

internal tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private const val MIN_BANNER_WIDTH = 1
