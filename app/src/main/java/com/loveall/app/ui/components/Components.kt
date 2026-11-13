package com.loveall.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.loveall.app.R
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.theme.*

@Composable
fun Page(title: String, back: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = AppDimensions.contentWidth)
                .fillMaxWidth()
                .padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            PageHeading(title, back)
            content()
        }
    }
}

@Composable
fun ListPage(title: String, back: (() -> Unit)? = null, content: LazyListScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            Modifier.widthIn(max = AppDimensions.contentWidth).fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            item { PageHeading(title, back) }
            content()
        }
    }
}

@Composable
fun PageHeading(title: String, back: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.compact),
    ) {
        if (back != null)
            FilledTonalIconButton(
                onClick = back,
                colors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
            }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny)) {
            Text(
                stringResource(R.string.brand_wordmark),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}

@Composable
fun InfoCard(title: String, body: String, content: @Composable ColumnScope.() -> Unit = {}) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier.padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@StringRes
fun AuthError.message(): Int =
    when (this) {
        AuthError.REQUIRED_CREDENTIALS -> R.string.required_credentials
        AuthError.INVALID_CREDENTIALS -> R.string.invalid_credentials
        AuthError.SESSION_EXPIRED -> R.string.session_expired
        AuthError.NETWORK -> R.string.network_error
        AuthError.STORAGE -> R.string.storage_error
        AuthError.VALIDATION -> R.string.validation_error
        AuthError.FORBIDDEN -> R.string.forbidden_error
        AuthError.RATE_LIMIT -> R.string.rate_limit_error
        AuthError.NOT_FOUND -> R.string.not_found_error
        AuthError.SERVICE -> R.string.service_error
    }

@Composable
fun ErrorCard(error: AuthError?, retry: (() -> Unit)? = null) {
    if (error == null) return
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
        Column(
            Modifier.fillMaxWidth().padding(AppSpacing.medium).semantics {
                liveRegion = LiveRegionMode.Polite
            },
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            Text(
                stringResource(error.message()),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (retry != null) TextButton(onClick = retry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
fun Loading() {
    Row(
        Modifier.fillMaxWidth().padding(AppSpacing.large),
        horizontalArrangement = Arrangement.Center,
    ) {
        val loadingLabel = stringResource(R.string.loading)
        CircularProgressIndicator(Modifier.semantics { contentDescription = loadingLabel })
    }
}

@Composable
fun AppField(
    value: String,
    onChange: (String) -> Unit,
    @StringRes label: Int,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        shape = MaterialTheme.shapes.small,
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        maxLines = if (singleLine) 1 else AppDimensions.textFieldMaxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
fun PrimaryButton(@StringRes text: Int, enabled: Boolean = true, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            if (pressed) AppDimensions.pressedScale else 1f,
            label = BUTTON_PRESS_ANIMATION_LABEL,
        )
    val palette = brandColors
    val fill =
        if (enabled) listOf(palette.heroStart, palette.heroEnd)
        else
            listOf(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.surfaceContainerHigh,
            )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(min = AppDimensions.buttonHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(Brush.horizontalGradient(fill)),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = palette.onHero,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Text(
            stringResource(text),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(AppDimensions.smallIcon))
    }
}

@Composable
fun Portrait(
    url: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = AppDimensions.avatar,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .padding(AppDimensions.border * 2),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (url != null && Rules.validProfilePhoto(url))
                AsyncImage(
                    model = photoModel(url),
                    contentDescription = stringResource(R.string.photo_of, name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
        }
    }
}

@Composable
fun ConfirmDialog(title: String, body: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) }
        },
    )
}

@Composable
fun GenderPicker(
    selected: List<String>,
    multiple: Boolean,
    onChange: (List<String>) -> Unit,
    enabled: Boolean = true,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Gender.entries.forEach { gender ->
            val checked = gender.wire in selected
            FilterChip(
                selected = checked,
                enabled = enabled,
                onClick = {
                    onChange(
                        if (!multiple) listOf(gender.wire)
                        else if (checked) selected - gender.wire else selected + gender.wire
                    )
                },
                label = { Text(stringResource(gender.label())) },
                leadingIcon =
                    if (checked)
                        ({
                            Icon(Icons.Default.Check, null, Modifier.size(AppDimensions.smallIcon))
                        })
                    else null,
                shape = CircleShape,
                modifier = Modifier.heightIn(min = AppDimensions.touchTarget),
            )
        }
    }
}

@StringRes
fun Gender.label() =
    when (this) {
        Gender.WOMAN -> R.string.gender_woman
        Gender.MAN -> R.string.gender_man
        Gender.NON_BINARY -> R.string.gender_non_binary
        Gender.TRANS_WOMAN -> R.string.gender_trans_woman
        Gender.TRANS_MAN -> R.string.gender_trans_man
        Gender.GENDERQUEER -> R.string.gender_queer
        Gender.GENDERFLUID -> R.string.gender_fluid
        Gender.AGENDER -> R.string.gender_agender
        Gender.BIGENDER -> R.string.gender_bigender
        Gender.TWO_SPIRIT -> R.string.gender_two_spirit
        Gender.OTHER -> R.string.gender_other
        Gender.PREFER_NOT_SAY -> R.string.gender_private
    }

@StringRes
fun ReportReason.label() =
    when (this) {
        ReportReason.FAKE -> R.string.reason_fake
        ReportReason.CONTENT -> R.string.reason_content
        ReportReason.HARASSMENT -> R.string.reason_harassment
        ReportReason.SPAM -> R.string.reason_spam
        ReportReason.UNDERAGE -> R.string.reason_underage
        ReportReason.SCAM -> R.string.reason_scam
        ReportReason.HATE -> R.string.reason_hate
        ReportReason.VIOLENCE -> R.string.reason_violence
        ReportReason.OTHER -> R.string.gender_other
    }

private const val BUTTON_PRESS_ANIMATION_LABEL = "buttonPress"
