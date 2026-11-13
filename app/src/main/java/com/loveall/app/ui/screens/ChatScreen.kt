package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.presentation.state.ChatState
import com.loveall.app.ui.components.ErrorCard
import com.loveall.app.ui.components.Loading
import com.loveall.app.ui.components.chat.ChatComposer
import com.loveall.app.ui.components.chat.ChatConfirmation
import com.loveall.app.ui.components.chat.ChatConfirmationDialog
import com.loveall.app.ui.components.chat.ChatFeedbackDialogs
import com.loveall.app.ui.components.chat.ChatHeader
import com.loveall.app.ui.components.chat.ChatMessageList
import com.loveall.app.ui.components.chat.ChatRefreshEffect
import com.loveall.app.ui.components.safety.ReportDialog
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
fun ChatScreen(
    state: ChatState,
    userId: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSend: (String) -> Unit,
    onOlder: () -> Unit,
    onBlock: () -> Unit,
    onUnmatch: () -> Unit,
    onReport: (ReportReason, String) -> Unit,
    onDismissFeedback: () -> Unit,
    onPlans: () -> Unit,
) {
    var confirmation by rememberSaveable { mutableStateOf<ChatConfirmation?>(null) }
    var report by rememberSaveable { mutableStateOf(false) }
    ChatRefreshEffect(onRefresh)
    LaunchedEffect(state.closed) { if (state.closed) onBack() }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val bubbleMaxWidth =
            maxWidth.coerceAtMost(AppDimensions.contentWidth) * AppDimensions.messageWidthFraction
        Column(Modifier.widthIn(max = AppDimensions.contentWidth).fillMaxSize().imePadding()) {
            ChatHeader(
                state = state,
                onBack = onBack,
                onReport = { report = true },
                onBlock = { confirmation = ChatConfirmation.BLOCK },
                onUnmatch = { confirmation = ChatConfirmation.UNMATCH },
            )
            ChatStatus(state, onRefresh, onPlans)
            ChatMessageList(
                state,
                userId,
                bubbleMaxWidth,
                onOlder,
                Modifier.weight(1f).fillMaxWidth(),
            )
            ChatComposer(
                !state.busy && !state.loading && state.partner != null,
                state.sentDraft,
                state.sendVersion,
                onSend,
            )
        }
    }
    ChatConfirmationDialog(confirmation, state.partner?.name.orEmpty(), { confirmation = null }) {
        val selected = confirmation
        confirmation = null
        if (selected == ChatConfirmation.BLOCK) onBlock() else onUnmatch()
    }
    if (report) ReportDialog({ report = false }, onReport)
    ChatFeedbackDialogs(state, onDismissFeedback)
}

@Composable
private fun ChatStatus(state: ChatState, onRefresh: () -> Unit, onPlans: () -> Unit) {
    Column(Modifier.padding(horizontal = AppSpacing.medium)) {
        ErrorCard(state.error, onRefresh)
        if (state.error == AuthError.FORBIDDEN)
            TextButton(onClick = onPlans) { Text(stringResource(R.string.plans)) }
        if (state.loading) Loading()
    }
}
