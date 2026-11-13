package com.loveall.app.ui.screens

import android.content.ActivityNotFoundException
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.SubscriptionOffer
import com.loveall.app.presentation.state.MonetizationState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.monetization.AdPrivacyButton

@Composable
fun AdFreeSubscriptionScreen(
    state: MonetizationState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRestore: () -> Unit,
    onPurchase: (SubscriptionOffer) -> Unit,
) {
    val uri = LocalUriHandler.current
    var browserFailed by remember { mutableStateOf(false) }
    Page(stringResource(R.string.ad_free_title), onBack) {
        Text(stringResource(R.string.ad_free_body))
        ErrorCard(state.error, onRefresh)
        if (state.busy) Loading()
        if (state.pending)
            InfoCard(
                stringResource(R.string.payment_title),
                stringResource(R.string.billing_pending),
            )
        if (state.entitlementKnown && state.entitlement.active) {
            InfoCard(
                stringResource(R.string.ad_free_active),
                stringResource(R.string.ad_free_active_body),
            )
        } else if (state.configured) {
            state.offers.forEach { offer ->
                InfoCard(
                    offer.name,
                    stringResource(
                        R.string.subscription_price,
                        offer.price,
                        subscriptionPeriod(offer.billingPeriod),
                    ),
                ) {
                    Text(stringResource(R.string.subscription_renewal))
                    PrimaryButton(
                        R.string.subscribe_play,
                        state.entitlementKnown && !state.busy && !state.pending,
                    ) {
                        onPurchase(offer)
                    }
                }
            }
            if (!state.busy && state.offers.isEmpty())
                Text(stringResource(R.string.billing_no_offers))
        } else Text(stringResource(R.string.billing_not_configured))
        if (state.configured) PrimaryButton(R.string.restore_purchases, !state.busy, onRestore)
        TextButton(
            onClick = {
                try {
                    uri.openUri(MANAGE_SUBSCRIPTIONS_URL)
                } catch (_: ActivityNotFoundException) {
                    browserFailed = true
                } catch (_: SecurityException) {
                    browserFailed = true
                }
            }
        ) {
            Text(stringResource(R.string.manage_subscription))
        }
        if (browserFailed) Text(stringResource(R.string.browser_unavailable))
        AdPrivacyButton()
        LegalDocuments()
    }
}

@Composable
private fun subscriptionPeriod(value: String): String {
    val period = runCatching { java.time.Period.parse(value) }.getOrNull()
    val months = period?.toTotalMonths()?.toInt() ?: 0
    val days = period?.days ?: 0
    return when {
        months > 0 && days == 0 && months % 12 == 0 ->
            androidx.compose.ui.res.pluralStringResource(
                R.plurals.subscription_years,
                months / 12,
                months / 12,
            )
        months > 0 && days == 0 ->
            androidx.compose.ui.res.pluralStringResource(
                R.plurals.subscription_months,
                months,
                months,
            )
        days > 0 && months == 0 && days % 7 == 0 ->
            androidx.compose.ui.res.pluralStringResource(
                R.plurals.subscription_weeks,
                days / 7,
                days / 7,
            )
        days > 0 && months == 0 ->
            androidx.compose.ui.res.pluralStringResource(R.plurals.subscription_days, days, days)
        else -> stringResource(R.string.subscription_period)
    }
}

private const val MANAGE_SUBSCRIPTIONS_URL = "https://play.google.com/store/account/subscriptions"
