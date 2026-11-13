package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.loveall.app.R
import com.loveall.app.domain.model.Message
import com.loveall.app.presentation.state.ChatState
import com.loveall.app.ui.components.EmptyIllustration
import com.loveall.app.ui.components.message
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ChatMessageList(
    state: ChatState,
    userId: String,
    bubbleMaxWidth: Dp,
    onOlder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val newestId = state.messages.lastOrNull()?.id
    LaunchedEffect(newestId) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex + 1)
    }
    LazyColumn(
        modifier,
        state = listState,
        contentPadding = PaddingValues(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.compact),
    ) {
        item {
            if (state.hasOlder && state.messages.isNotEmpty())
                TextButton(
                    onClick = onOlder,
                    enabled = !state.busy && !state.loadingOlder,
                ) {
                    Text(stringResource(R.string.older_messages))
                }
            if (state.messages.isEmpty() && !state.loading)
                EmptyIllustration(
                    stringResource(R.string.chat_empty_title),
                    stringResource(R.string.empty_chat),
                    Icons.Default.Email,
                )
        }
        items(state.messages, key = Message::id) { message ->
            val own = message.senderId == userId
            ChatMessageBubble(message, own, bubbleMaxWidth)
        }
    }
}
