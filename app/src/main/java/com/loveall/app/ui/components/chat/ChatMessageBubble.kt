package com.loveall.app.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.loveall.app.R
import com.loveall.app.domain.model.Message
import com.loveall.app.presentation.formatMessageTime
import com.loveall.app.ui.components.message
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing
import com.loveall.app.ui.theme.brandColors
import java.time.ZoneId

@Composable
internal fun ChatMessageBubble(message: Message, own: Boolean, bubbleMaxWidth: Dp) {
    val locale = LocalConfiguration.current.locales[0]
    val zone = remember { ZoneId.systemDefault() }
    val palette = brandColors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (own) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape =
                RoundedCornerShape(
                    topStart = AppDimensions.cardRadius,
                    topEnd = AppDimensions.cardRadius,
                    bottomStart = if (own) AppDimensions.cardRadius else AppSpacing.tiny,
                    bottomEnd = if (own) AppSpacing.tiny else AppDimensions.cardRadius,
                ),
            color = if (own) palette.heroStart else MaterialTheme.colorScheme.surface,
            contentColor = if (own) palette.onHero else MaterialTheme.colorScheme.onSurface,
            border =
                if (own) null
                else
                    BorderStroke(
                        AppDimensions.border,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
        ) {
            Column(
                Modifier.widthIn(max = bubbleMaxWidth).padding(AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
            ) {
                Text(message.content, style = MaterialTheme.typography.bodyLarge)
                formatMessageTime(message.createdAt, locale, zone)?.let { time ->
                    val description = stringResource(R.string.sent_at, time)
                    Text(
                        time,
                        style = MaterialTheme.typography.labelSmall,
                        modifier =
                            Modifier.align(Alignment.End).semantics {
                                contentDescription = description
                            },
                    )
                }
            }
        }
    }
}
