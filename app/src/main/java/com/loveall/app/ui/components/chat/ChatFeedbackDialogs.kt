package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.presentation.state.ChatState

@Composable
internal fun ChatFeedbackDialogs(state: ChatState, onDismissFeedback: () -> Unit) {
    if (state.reported)
        AlertDialog(
            onDismissRequest = onDismissFeedback,
            icon = {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary)
            },
            text = { Text(stringResource(R.string.report_sent)) },
            confirmButton = {
                TextButton(onClick = onDismissFeedback) {
                    Text(stringResource(R.string.continue_action))
                }
            },
        )
    state.eligibleMinutes?.let { minutes ->
        AlertDialog(
            onDismissRequest = onDismissFeedback,
            title = { Text(stringResource(R.string.call_integration_title)) },
            text = {
                Text(pluralStringResource(R.plurals.call_integration_body, minutes, minutes))
            },
            confirmButton = {
                TextButton(onClick = onDismissFeedback) {
                    Text(stringResource(R.string.continue_action))
                }
            },
        )
    }
}
