package com.loveall.app.ui.components.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.components.PrimaryButton
import com.loveall.app.ui.components.Tag
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun PlanOffer(
    plan: Plan,
    price: String,
    externalCheckoutEnabled: Boolean,
    checkoutEnabled: Boolean,
    onCheckout: (Plan) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        Modifier.padding(AppSpacing.compact),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                price,
                style = MaterialTheme.typography.displaySmall,
            )
            Tag(
                if (plan.minutes >= Rules.UNLIMITED_MINUTES)
                    stringResource(R.string.unlimited_minutes)
                else
                    pluralStringResource(
                        R.plurals.plan_minutes,
                        plan.minutes,
                        plan.minutes,
                    ),
                icon = Icons.Default.Check,
            )
            Text(
                stringResource(R.string.plan_period),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (externalCheckoutEnabled)
                PrimaryButton(
                    R.string.subscribe,
                    checkoutEnabled,
                ) {
                    onCheckout(plan)
                }
        }
    }
}
