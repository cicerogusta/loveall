package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.presentation.*
import com.loveall.app.presentation.state.DetailState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.components.discovery.ProfileCard
import com.loveall.app.ui.components.safety.ReportDialog
import com.loveall.app.ui.theme.*

@Composable
fun DetailScreen(
    state: DetailState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onBlock: () -> Unit,
    onReport: (ReportReason, String) -> Unit,
) {
    var blockDialog by rememberSaveable { mutableStateOf(false) }
    var reportDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.blocked) { if (state.blocked) onBack() }
    Page(stringResource(R.string.profile), onBack) {
        ErrorCard(state.error, onRefresh)
        if (state.loading) Loading()
        state.profile?.let { profile ->
            ProfileCard(profile)
            SectionTitle(stringResource(R.string.safety))
            OutlinedButton(
                onClick = { blockDialog = true },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
            ) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(AppSpacing.small))
                Text(stringResource(R.string.block))
            }
            TextButton(onClick = { reportDialog = true }, enabled = !state.busy) {
                Icon(Icons.Default.Info, null)
                Spacer(Modifier.width(AppSpacing.small))
                Text(stringResource(R.string.report))
            }
            if (state.reported) Text(stringResource(R.string.report_sent))
            if (blockDialog)
                ConfirmDialog(
                    stringResource(R.string.block),
                    stringResource(R.string.block_body, profile.name),
                    { blockDialog = false },
                    {
                        blockDialog = false
                        onBlock()
                    },
                )
        }
    }
    if (reportDialog) ReportDialog({ reportDialog = false }, onReport)
}
