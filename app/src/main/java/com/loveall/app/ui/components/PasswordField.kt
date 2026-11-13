package com.loveall.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.loveall.app.R
import com.loveall.app.ui.theme.*

@Composable
internal fun PasswordField(value: String, onChange: (String) -> Unit, enabled: Boolean) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(
        value,
        onChange,
        label = { Text(stringResource(R.string.password)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        leadingIcon = {
            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        visualTransformation =
            if (show) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = { show = !show }) {
                Text(
                    stringResource(if (show) R.string.hide_password else R.string.show_password),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
    )
}
