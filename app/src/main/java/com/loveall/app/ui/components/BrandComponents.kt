package com.loveall.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.loveall.app.R
import com.loveall.app.ui.theme.*

@Composable
fun BrandWordmark(modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Box(Modifier.size(AppDimensions.brandMark), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Favorite,
                    null,
                    Modifier.size(AppDimensions.smallIcon),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Text(stringResource(R.string.brand_wordmark), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun HeroSurface(content: @Composable ColumnScope.() -> Unit) {
    val accents = brandColors
    Surface(
        shape = MaterialTheme.shapes.large,
        color = accents.ink,
        contentColor = accents.onHero,
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(accents.heroStart, accents.heroEnd)))
        ) {
            Canvas(Modifier.matchParentSize().clearAndSetSemantics {}) {
                drawCircle(
                    accents.onHero.copy(alpha = 0.08f),
                    size.width * 0.43f,
                    Offset(size.width * 0.95f, size.height * 0.15f),
                )
                drawCircle(
                    accents.onHero.copy(alpha = 0.06f),
                    size.width * 0.27f,
                    Offset(size.width * 0.1f, size.height * 1.05f),
                )
            }
            Column(
                Modifier.fillMaxWidth().padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                content = content,
            )
        }
    }
}

@Composable
fun Eyebrow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
    ) {
        Box(
            Modifier.size(AppSpacing.small)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    highlighted: Boolean = false,
) {
    Surface(
        modifier,
        shape = CircleShape,
        color =
            if (highlighted) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            Modifier.padding(horizontal = AppSpacing.compact, vertical = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
        ) {
            if (icon != null) Icon(icon, null, Modifier.size(AppDimensions.smallIcon))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun FeatureTile(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    icon,
                    null,
                    Modifier.padding(AppSpacing.compact).size(AppDimensions.icon),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}

@Composable
fun FormSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.compact)) {
        SectionTitle(title, subtitle)
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.compact),
                content = content,
            )
        }
    }
}

@Composable
fun MenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EmptyIllustration(
    title: String,
    body: String,
    icon: ImageVector = Icons.Default.FavoriteBorder,
) {
    val accents = brandColors
    Column(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
    ) {
        Box(Modifier.size(AppDimensions.compactPhoto), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
                drawCircle(
                    accents.peach,
                    radius = size.minDimension * 0.46f,
                    center = center + Offset(-size.width * 0.06f, size.height * 0.04f),
                )
                drawCircle(
                    accents.lavender,
                    radius = size.minDimension * 0.32f,
                    center = center + Offset(size.width * 0.17f, -size.height * 0.15f),
                )
            }
            Icon(
                icon,
                null,
                Modifier.size(AppDimensions.touchTarget),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Original vector artwork: expressive brand illustration, never a fabricated user profile. */
@Composable
fun ConnectionArtwork(modifier: Modifier = Modifier) {
    val accents = brandColors
    val shape = MaterialTheme.shapes.large
    Box(
        modifier.fillMaxWidth().height(AppDimensions.artworkHeight).clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawOval(
                accents.peach,
                topLeft = Offset(size.width * 0.06f, size.height * 0.05f),
                size = Size(size.width * 0.85f, size.height * 0.89f),
            )
            drawCircle(
                accents.lavender,
                radius = size.height * 0.22f,
                center = Offset(size.width * 0.86f, size.height * 0.2f),
            )
            drawCircle(
                accents.mint,
                radius = size.height * 0.12f,
                center = Offset(size.width * 0.12f, size.height * 0.78f),
            )
            val path =
                Path().apply {
                    moveTo(size.width * 0.02f, size.height * 0.44f)
                    cubicTo(
                        size.width * 0.1f,
                        -size.height * 0.22f,
                        size.width,
                        size.height * 0.28f,
                        size.width * 0.95f,
                        size.height * 0.79f,
                    )
                }
            drawPath(
                path,
                accents.ink.copy(alpha = 0.17f),
                style = Stroke(width = size.width * 0.003f),
            )
        }
        Box(
            Modifier.offset(x = -AppDimensions.actionCircle)
                .rotate(-12f)
                .size(AppDimensions.illustrationCardWidth, AppDimensions.illustrationCardHeight)
                .shadow(AppDimensions.elevation, shape)
                .clip(shape)
                .background(accents.lavender)
        ) {
            Canvas(Modifier.fillMaxSize()) { character(accents, false) }
        }
        Box(
            Modifier.offset(x = AppDimensions.avatar, y = AppSpacing.small)
                .rotate(10f)
                .size(AppDimensions.illustrationCardWidth, AppDimensions.illustrationCardHeight)
                .shadow(AppDimensions.elevation, shape)
                .clip(shape)
                .background(accents.rose)
        ) {
            Canvas(Modifier.fillMaxSize()) { character(accents, true) }
        }
        Surface(
            Modifier.align(Alignment.BottomCenter).offset(y = -AppSpacing.small).rotate(-5f),
            shape = CircleShape,
            shadowElevation = AppDimensions.elevation,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                Modifier.padding(horizontal = AppSpacing.medium, vertical = AppSpacing.compact),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            ) {
                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.artwork_caption),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private fun DrawScope.character(colors: BrandColors, second: Boolean) {
    withTransform({ scale(size.width / 100f, size.height / 140f, pivot = Offset.Zero) }) {
        val skin = if (second) colors.deepSkin else colors.warmSkin
        val shirt = if (second) colors.ink else colors.heroStart
        drawCircle(colors.hair, 28f, Offset(51f, 49f))
        if (second) {
            drawCircle(colors.hair, 13f, Offset(32f, 29f))
            drawCircle(colors.hair, 15f, Offset(62f, 24f))
        }
        val shoulders =
            Path().apply {
                moveTo(4f, 140f)
                cubicTo(0f, 91f, 20f, 87f, 37f, 85f)
                lineTo(64f, 85f)
                cubicTo(85f, 90f, 103f, 100f, 104f, 140f)
                close()
            }
        drawPath(shoulders, shirt)
        drawRoundRect(
            skin,
            Offset(42f, 69f),
            Size(19f, 26f),
            androidx.compose.ui.geometry.CornerRadius(8f),
        )
        drawOval(skin, Offset(31f, 29f), Size(42f, 52f))
        val hair =
            Path().apply {
                moveTo(27f, 52f)
                cubicTo(15f, 2f, 88f, 10f, 79f, 52f)
                cubicTo(61f, 48f, 61f, 30f, 49f, 29f)
                cubicTo(44f, 43f, 37f, 43f, 27f, 52f)
                close()
            }
        drawPath(hair, colors.hair)
        drawCircle(colors.hair, 1.5f, Offset(43f, 55f))
        drawCircle(colors.hair, 1.5f, Offset(61f, 55f))
        drawArc(
            colors.hair,
            12f,
            155f,
            false,
            Offset(44f, 60f),
            Size(16f, 10f),
            style = Stroke(1.8f, cap = StrokeCap.Round),
        )
        if (!second) drawOval(colors.onInk, Offset(39f, 83f), Size(28f, 12f))
        drawLine(colors.onInk.copy(alpha = 0.3f), Offset(25f, 112f), Offset(77f, 112f), 1.5f)
    }
}
