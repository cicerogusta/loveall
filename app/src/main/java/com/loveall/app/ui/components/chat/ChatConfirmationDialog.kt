package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.components.ConfirmDialog

@Composable
internal fun ChatConfirmationDialog(
    action: ChatConfirmation?,
    partnerName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (action == null) return
    val blocking = action == ChatConfirmation.BLOCK
    ConfirmDialog(
        stringResource(if (blocking) R.string.block else R.string.unmatch),
        if (blocking) stringResource(R.string.block_body, partnerName)
        else stringResource(R.string.unmatch_body),
        onDismiss,
        onConfirm,
    )
}
