package com.loveall.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.loveall.app.R
import com.loveall.app.domain.model.Match
import com.loveall.app.presentation.*
import com.loveall.app.presentation.state.MatchesState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun MatchesScreen(
    state: MatchesState,
    onRefresh: () -> Unit,
    onChat: (String) -> Unit,
) {
    ListPage(stringResource(R.string.matches)) {
        item {
            Text(
                stringResource(R.string.matches_support),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ErrorCard(state.error, onRefresh)
            if (state.loading) Loading()
            TextButton(onClick = onRefresh, enabled = !state.loading) {
                Icon(Icons.Default.Refresh, null, Modifier.size(AppDimensions.smallIcon))
                Spacer(Modifier.width(AppSpacing.small))
                Text(stringResource(R.string.refresh))
            }
            if (!state.loading && state.matches.isEmpty() && state.error == null)
                EmptyIllustration(
                    stringResource(R.string.empty_matches_title),
                    stringResource(R.string.empty_matches),
                )
        }
        if (state.matches.isNotEmpty())
            item { SectionTitle(stringResource(R.string.matches_section)) }
        items(state.matches, key = Match::id) { match ->
            Card(
                onClick = { onChat(match.id) },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border =
                    BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    Modifier.padding(AppSpacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Portrait(match.partner.photos.firstOrNull(), match.partner.name)
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
                    ) {
                        Text(match.partner.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            match.lastMessage ?: stringResource(R.string.new_match),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (match.unread > 0)
                            Text(
                                pluralStringResource(
                                    R.plurals.unread_messages,
                                    match.unread,
                                    match.unread,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                    }
                    Surface(
                        shape = CircleShape,
                        color =
                            if (match.unread > 0) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            Modifier.padding(AppSpacing.small),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
