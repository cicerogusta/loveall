package com.loveall.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.core.text.TextDefaults
import com.loveall.app.domain.error.AuthError

@Composable
fun DeleteAccountDialog(
    busy: Boolean,
    error: AuthError?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    // A senha não é persistida no estado salvo da Activity.
    var password by remember { mutableStateOf(TextDefaults.EMPTY) }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.delete_account)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.delete_account_body))
                PasswordField(password, { password = it }, !busy)
                if (error == AuthError.FORBIDDEN)
                    Text(stringResource(R.string.delete_account_password_error))
                else ErrorCard(error)
                if (busy) Loading()
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = !busy && password.isNotBlank(),
            ) {
                Text(
                    stringResource(R.string.delete_account),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
