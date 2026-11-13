package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.core.format.BrazilianLocale
import com.loveall.app.domain.model.Plan
import com.loveall.app.presentation.state.PlansState
import com.loveall.app.ui.components.ErrorCard
import com.loveall.app.ui.components.FeatureTile
import com.loveall.app.ui.components.ListPage
import com.loveall.app.ui.components.Loading
import com.loveall.app.ui.components.plans.CheckoutBrowserEffect
import com.loveall.app.ui.components.plans.PaymentStatusSection
import com.loveall.app.ui.components.plans.PlanOffer
import com.loveall.app.ui.components.plans.PlansHero
import com.loveall.app.ui.components.plans.SubscriptionBalance
import java.text.NumberFormat
import java.util.Currency

@Composable
fun PlansScreen(
    state: PlansState,
    externalCheckoutEnabled: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCheckout: (Plan) -> Unit,
    onCheckoutOpened: () -> Unit,
    onCheckPayment: () -> Unit,
) {
    var browserError by remember { mutableStateOf(false) }
    if (externalCheckoutEnabled)
        CheckoutBrowserEffect(state.checkout, onCheckoutOpened) { browserError = true }
    val formatter = remember {
        NumberFormat.getCurrencyInstance(BrazilianLocale.value).apply {
            currency = Currency.getInstance(BrazilianLocale.CURRENCY_CODE)
        }
    }
    ListPage(stringResource(R.string.plans), onBack) {
        item {
            ErrorCard(state.error, onRefresh)
            if (state.loading) Loading()
            if (browserError) Text(stringResource(R.string.browser_unavailable))
            if (externalCheckoutEnabled) PlansHero()
        }
        state.subscription?.let { sub ->
            item {
                SubscriptionBalance(sub, state.plans, !state.busy && !state.loading, onRefresh)
            }
        }
        item {
            FeatureTile(
                Icons.Default.Info,
                stringResource(R.string.calls_information),
                stringResource(R.string.calls_unavailable),
            )
            if (!externalCheckoutEnabled)
                Text(
                    stringResource(R.string.checkout_disabled),
                    style = MaterialTheme.typography.bodyMedium,
                )
        }
        items(if (externalCheckoutEnabled) state.plans else emptyList(), key = Plan::id) { plan ->
            PlanOffer(
                plan,
                formatter.format(plan.price),
                externalCheckoutEnabled,
                !state.busy && !state.loading && state.pendingTransaction == null,
                onCheckout,
            )
        }
        item {
            FeatureTile(
                Icons.Default.FavoriteBorder,
                stringResource(R.string.free_plan),
                stringResource(R.string.free_likes),
            )
        }
        if (state.pendingTransaction != null || state.payment != null)
            item {
                PaymentStatusSection(
                    state.payment?.status,
                    state.pendingTransaction != null,
                    !state.busy && !state.loading,
                    onCheckPayment,
                )
            }
    }
}
