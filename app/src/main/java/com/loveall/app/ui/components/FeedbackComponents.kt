package com.loveall.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.loveall.app.R
import com.loveall.app.domain.error.AuthError
import com.loveall.app.ui.theme.*

@StringRes
fun AuthError.message(): Int =
    when (this) {
        AuthError.REQUIRED_CREDENTIALS -> R.string.required_credentials
        AuthError.INVALID_CREDENTIALS -> R.string.invalid_credentials
        AuthError.SESSION_EXPIRED -> R.string.session_expired
        AuthError.NETWORK -> R.string.network_error
        AuthError.STORAGE -> R.string.storage_error
        AuthError.VALIDATION -> R.string.validation_error
        AuthError.FORBIDDEN -> R.string.forbidden_error
        AuthError.RATE_LIMIT -> R.string.rate_limit_error
        AuthError.NOT_FOUND -> R.string.not_found_error
        AuthError.SERVICE -> R.string.service_error
    }

@Composable
fun ErrorCard(error: AuthError?, retry: (() -> Unit)? = null) {
    if (error == null) return
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
        Column(
            Modifier.fillMaxWidth().padding(AppSpacing.medium).semantics {
                liveRegion = LiveRegionMode.Polite
            },
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            Text(
                stringResource(error.message()),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (retry != null) TextButton(onClick = retry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
fun Loading() {
    Row(
        Modifier.fillMaxWidth().padding(AppSpacing.large),
        horizontalArrangement = Arrangement.Center,
    ) {
        val loadingLabel = stringResource(R.string.loading)
        CircularProgressIndicator(Modifier.semantics { contentDescription = loadingLabel })
    }
}
