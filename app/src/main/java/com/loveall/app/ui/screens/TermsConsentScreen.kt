package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.presentation.state.SessionState
import com.loveall.app.ui.components.*

/** Contas anteriores à versão atual também precisam conhecer as regras antes de publicar. */
@Composable
fun TermsConsentScreen(
    state: SessionState,
    onAccept: () -> Unit,
    onLogout: () -> Unit,
    onDelete: (String) -> Unit,
) {
    var accepted by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    Scaffold { insets ->
        Box(Modifier.padding(insets)) {
            Page(stringResource(R.string.terms_title)) {
                Text(stringResource(R.string.terms_consent_body))
                LegalDocuments()
                Row {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                        enabled = !state.busy,
                    )
                    Text(stringResource(R.string.accept_terms))
                }
                ErrorCard(state.error)
                if (state.busy) Loading()
                PrimaryButton(R.string.continue_action, accepted && !state.busy, onAccept)
                TextButton(onClick = onLogout, enabled = !state.busy) {
                    Text(stringResource(R.string.logout))
                }
                TextButton(onClick = { deleting = true }, enabled = !state.busy) {
                    Text(stringResource(R.string.delete_account))
                }
            }
        }
    }
    if (deleting) DeleteAccountDialog(state.busy, state.error, { deleting = false }, onDelete)
}
