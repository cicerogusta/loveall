package com.loveall.app.ui.monetization

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loveall.app.R

@Composable
fun AdPrivacyButton() {
    val controller = LocalAdConsent.current ?: return
    val state by controller.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity() ?: return
    if (state.failed) Text(stringResource(R.string.ads_privacy_error))
    if (state.privacyOptionsRequired)
        TextButton(onClick = { controller.showPrivacyOptions(activity) }) {
            Text(stringResource(R.string.ads_privacy_options))
        }
}
