package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.loveall.app.R
import com.loveall.app.core.text.TextDefaults
import com.loveall.app.presentation.state.SessionState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun LoginScreen(
    state: SessionState,
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(TextDefaults.EMPTY) }
    // Passwords deliberately stay out of saved instance state and disk.
    var password by remember { mutableStateOf(TextDefaults.EMPTY) }
    Page(stringResource(R.string.login_title), onBack) {
        Eyebrow(stringResource(R.string.login_kicker))
        Text(
            stringResource(R.string.login_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ConnectionArtwork(Modifier)
        ErrorCard(state.error)
        AppField(
            email,
            { email = it },
            R.string.email,
            !state.busy,
            keyboardType = KeyboardType.Email,
        )
        PasswordField(password, { password = it }, !state.busy)
        PrimaryButton(if (state.busy) R.string.signing_in else R.string.submit_login, !state.busy) {
            onLogin(email, password)
        }
        TextButton(
            onClick = onRegister,
            enabled = !state.busy,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.create_account))
        }
        Text(
            stringResource(R.string.auth_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
