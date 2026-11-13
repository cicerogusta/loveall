package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.components.ConnectionArtwork
import com.loveall.app.ui.theme.AppDimensions

@Composable
internal fun ExploreMatchDialog(
    matchedName: String,
    onDismiss: () -> Unit,
    onOpenChat: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Favorite,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(AppDimensions.touchTarget),
            )
        },
        title = {
            Text(
                stringResource(R.string.match_title),
                style = MaterialTheme.typography.headlineLarge,
            )
        },
        text = {
            Column {
                ConnectionArtwork()
                Text(stringResource(R.string.match_body, matchedName))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onOpenChat()
                }
            ) {
                Text(stringResource(R.string.open_chat))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.continue_action)) }
        },
    )
}
