package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.loveall.app.R
import com.loveall.app.core.format.BrazilianLocale
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = AppDimensions.formWidth)
                .fillMaxWidth()
                .padding(AppSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BrandWordmark()
                Text(
                    stringResource(R.string.tagline),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ConnectionArtwork()
            Eyebrow(stringResource(R.string.welcome_kicker))
            Text(
                stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.welcome_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
            ) {
                Tag(stringResource(R.string.welcome_inclusive), icon = Icons.Default.FavoriteBorder)
                Tag(
                    stringResource(R.string.welcome_at_your_pace),
                    icon = Icons.Default.Check,
                    highlighted = true,
                )
            }
            LegalDocuments()
            Spacer(Modifier.height(AppSpacing.tiny))
            PrimaryButton(R.string.create_account, onClick = onRegister)
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.buttonHeight),
                shape = CircleShape,
                border =
                    androidx.compose.foundation.BorderStroke(
                        AppDimensions.border,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
            ) {
                Text(stringResource(R.string.sign_in), color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                stringResource(R.string.auth_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, locale = BrazilianLocale.PREVIEW_TAG)
@Composable
private fun WelcomePreview() {
    LoveAllTheme { WelcomeScreen({}, {}) }
}
