package com.loveall.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.domain.model.PaymentState
import com.loveall.app.domain.model.PaymentStatus
import com.loveall.app.domain.model.Plan
import com.loveall.app.presentation.state.PlansState
import com.loveall.app.ui.screens.PlansScreen
import com.loveall.app.ui.theme.LoveAllTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PlansSectionsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val plan = Plan("basic", "Básico", 9.9, 60)

    @Test
    fun enabledOfferDelegatesCheckoutForSelectedPlan() {
        var selected: Plan? = null
        content(PlansState(plans = listOf(plan)), true, { selected = it })
        scrollTo(context.getString(R.string.subscribe))
        compose.onNodeWithText(context.getString(R.string.subscribe)).performClick()
        assertEquals(plan, selected)
    }

    @Test
    fun disabledExternalCheckoutHidesOffersAndPurchaseButton() {
        content(PlansState(plans = listOf(plan)), false)
        scrollTo(context.getString(R.string.checkout_disabled))
        compose.onNodeWithText(plan.name).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.subscribe)).assertDoesNotExist()
    }

    @Test
    fun pendingPaymentDisablesNewPurchaseAndDelegatesStatusCheck() {
        var checks = 0
        content(
            PlansState(
                plans = listOf(plan),
                pendingTransaction = "pending-test",
                payment = PaymentStatus(PaymentState.PENDING, "basic"),
            ),
            true,
            onCheck = { checks++ },
        )
        scrollTo(context.getString(R.string.subscribe))
        compose.onNodeWithText(context.getString(R.string.subscribe)).assertIsNotEnabled()
        scrollTo(context.getString(R.string.check_payment))
        compose.onNodeWithText(context.getString(R.string.check_payment)).performClick()
        assertEquals(1, checks)
    }

    private fun content(
        state: PlansState,
        enabled: Boolean,
        onCheckout: (Plan) -> Unit = {},
        onCheck: () -> Unit = {},
    ) {
        compose.setContent {
            LoveAllTheme { PlansScreen(state, enabled, {}, {}, onCheckout, {}, onCheck) }
        }
    }

    private fun scrollTo(text: String) {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    }
}
