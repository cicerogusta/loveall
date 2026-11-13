package com.loveall.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.domain.model.AdFreeEntitlement
import com.loveall.app.domain.model.SubscriptionOffer
import com.loveall.app.presentation.state.MonetizationState
import com.loveall.app.ui.screens.AdFreeSubscriptionScreen
import com.loveall.app.ui.theme.LoveAllTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AdFreeSubscriptionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val offer =
        SubscriptionOffer("offer-test", "product-test", "Sem anúncios mensal", "R$ 12,90", "P1M")

    @Test
    fun missingConfigurationHidesPurchaseAndShowsAvailabilityMessage() {
        content(MonetizationState())
        compose.onNodeWithText(label(R.string.billing_not_configured)).assertExists()
        compose.onNodeWithText(label(R.string.subscribe_play)).assertDoesNotExist()
    }

    @Test
    fun storeOfferDisplaysPricePeriodAndRenewalBeforeDelegatingPurchase() {
        var selected: SubscriptionOffer? = null
        content(
            MonetizationState(configured = true, entitlementKnown = true, offers = listOf(offer)),
            purchase = { selected = it },
        )
        compose
            .onNodeWithText(
                context.getString(
                    R.string.subscription_price,
                    offer.price,
                    context.resources.getQuantityString(R.plurals.subscription_months, 1, 1),
                )
            )
            .assertExists()
        compose.onNodeWithText(label(R.string.subscription_renewal)).assertExists()
        compose.onNodeWithText(label(R.string.subscribe_play)).performScrollTo().performClick()
        assertEquals(offer, selected)
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory =
            java.io.File(context.getExternalFilesDir(null), "qa/monetization").apply { mkdirs() }
        java.io.File(directory, "ad-free-offer.png").outputStream().use {
            image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun activeSubscriptionHidesPurchaseAndExposesManagement() {
        content(
            MonetizationState(
                configured = true,
                entitlementKnown = true,
                entitlement = AdFreeEntitlement(true, "2027-01-01T00:00:00Z"),
                offers = listOf(offer),
            )
        )
        compose.onNodeWithText(label(R.string.ad_free_active)).assertExists()
        compose.onNodeWithText(label(R.string.subscribe_play)).assertDoesNotExist()
        compose
            .onNodeWithText(label(R.string.manage_subscription))
            .performScrollTo()
            .assertHasClickAction()
    }

    @Test
    fun pendingPaymentCannotStartAnotherPurchaseAndAllowsRestoration() {
        var restores = 0
        content(
            MonetizationState(
                configured = true,
                entitlementKnown = true,
                pending = true,
                offers = listOf(offer),
            ),
            restore = { restores++ },
        )
        compose
            .onNodeWithText(label(R.string.subscribe_play))
            .performScrollTo()
            .assertIsNotEnabled()
        compose.onNodeWithText(label(R.string.restore_purchases)).performScrollTo().performClick()
        assertEquals(1, restores)
    }

    @Test
    fun unknownEntitlementCannotStartPurchase() {
        content(MonetizationState(configured = true, offers = listOf(offer)))
        compose
            .onNodeWithText(label(R.string.subscribe_play))
            .performScrollTo()
            .assertIsNotEnabled()
    }

    private fun content(
        state: MonetizationState,
        purchase: (SubscriptionOffer) -> Unit = {},
        restore: () -> Unit = {},
    ) {
        compose.setContent {
            LoveAllTheme { AdFreeSubscriptionScreen(state, {}, {}, restore, purchase) }
        }
    }

    private fun label(id: Int) = context.getString(id)
}
