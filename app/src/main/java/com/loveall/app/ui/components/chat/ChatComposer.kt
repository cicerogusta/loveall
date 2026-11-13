package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.core.text.TextDefaults
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.components.label
import com.loveall.app.ui.components.message
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

/**
 * Mantém o texto ainda não enviado como estado visual restaurável. A confirmação do ViewModel
 * identifica qual rascunho foi aceito; somente esse texto é limpo. Um novo texto digitado depois da
 * confirmação correspondente não deve ser apagado.
 */
@Composable
internal fun ChatComposer(
    enabled: Boolean,
    sentDraft: String?,
    sendVersion: Long,
    onSend: (String) -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf(TextDefaults.EMPTY) }
    LaunchedEffect(sendVersion) {
        if (sendVersion > 0 && draft == sentDraft) draft = TextDefaults.EMPTY
    }
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().padding(AppSpacing.medium),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.compact),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { if (it.length <= Rules.MAX_MESSAGE_LENGTH) draft = it },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                maxLines = AppDimensions.textFieldMaxLines,
                label = { Text(stringResource(R.string.message)) },
                shape = MaterialTheme.shapes.medium,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
            )
            FilledIconButton(
                onClick = { onSend(draft) },
                enabled = enabled && draft.isNotBlank(),
                modifier = Modifier.size(AppDimensions.buttonHeight),
                shape = CircleShape,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.send))
            }
        }
    }
}
