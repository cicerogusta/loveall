package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.presentation.state.ChatState

@Composable
internal fun ChatActionMenu(
    state: ChatState,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onUnmatch: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menu = true }) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.chat_more))
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.calls_menu_unavailable)) },
                leadingIcon = { Icon(Icons.Default.Call, null) },
                enabled = false,
                onClick = {},
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.report)) },
                leadingIcon = { Icon(Icons.Default.Info, null) },
                enabled = !state.busy,
                onClick = {
                    menu = false
                    onReport()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.block)) },
                leadingIcon = { Icon(Icons.Default.Close, null) },
                enabled = !state.busy,
                onClick = {
                    menu = false
                    onBlock()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.unmatch)) },
                enabled = !state.busy,
                onClick = {
                    menu = false
                    onUnmatch()
                },
            )
        }
    }
}
