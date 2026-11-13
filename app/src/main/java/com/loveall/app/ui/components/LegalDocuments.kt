package com.loveall.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R

private enum class LegalDocument(@StringRes val title: Int, @StringRes val body: Int) {
    TERMS(R.string.terms_title, R.string.terms_body),
    PRIVACY(R.string.privacy_title, R.string.privacy_body),
    SAFETY(R.string.child_safety_title, R.string.child_safety_body),
}

/** Documentos locais continuam acessíveis mesmo quando o serviço está indisponível. */
@Composable
fun LegalDocuments() {
    var selected by remember { mutableStateOf<LegalDocument?>(null) }
    Column {
        LegalDocument.entries.forEach { document ->
            TextButton(onClick = { selected = document }) { Text(stringResource(document.title)) }
        }
    }
    selected?.let { document ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(stringResource(document.title)) },
            text = {
                Text(stringResource(document.body), Modifier.verticalScroll(rememberScrollState()))
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}
