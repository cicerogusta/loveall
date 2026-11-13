package com.loveall.app.ui.components.plans

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.model.PlanIds
import com.loveall.app.domain.model.Subscription
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun SubscriptionBalance(
    sub: Subscription,
    plans: List<Plan>,
    enabled: Boolean,
    onRefresh: () -> Unit,
) {
    val name =
        if (sub.plan == PlanIds.FREE) stringResource(R.string.free_plan)
        else plans.firstOrNull { it.id == sub.plan }?.name ?: sub.plan
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            Text(
                stringResource(R.string.plan_balance),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                stringResource(R.string.current_plan, name),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                if (sub.minutesRemaining >= Rules.UNLIMITED_MINUTES)
                    stringResource(R.string.unlimited_minutes)
                else
                    pluralStringResource(
                        R.plurals.remaining_minutes,
                        sub.minutesRemaining,
                        sub.minutesRemaining,
                    )
            )
            TextButton(onClick = onRefresh, enabled = enabled) {
                Icon(
                    Icons.Default.Refresh,
                    null,
                    Modifier.size(AppDimensions.smallIcon),
                )
                Spacer(Modifier.width(AppSpacing.small))
                Text(stringResource(R.string.refresh))
            }
        }
    }
}
