package com.loveall.app.ui.components.safety

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.loveall.app.R
import com.loveall.app.core.text.TextDefaults
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.presentation.*
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun ReportDialog(onDismiss: () -> Unit, onReport: (ReportReason, String) -> Unit) {
    var selected by remember { mutableStateOf(ReportReason.FAKE) }
    var description by remember { mutableStateOf(TextDefaults.EMPTY) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.report)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.report_body))
                ReportReason.entries.forEach { reason ->
                    Row(
                        Modifier.fillMaxWidth()
                            .heightIn(min = AppDimensions.touchTarget)
                            .selectable(
                                selected = selected == reason,
                                role = Role.RadioButton,
                                onClick = { selected = reason },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == reason, onClick = null)
                        Text(stringResource(reason.label()))
                    }
                }
                AppField(
                    description,
                    { description = it },
                    R.string.report_description,
                    singleLine = false,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onReport(selected, description)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.report))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
