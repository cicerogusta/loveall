package com.loveall.app.domain

import com.loveall.app.domain.validation.AdEligibility
import org.junit.Assert.*
import org.junit.Test

class AdEligibilityTest {
    @Test
    fun `ads require known free entitlement and no pending purchase`() {
        assertTrue(AdEligibility.canDisplay(true, false, false))
        assertFalse(AdEligibility.canDisplay(false, false, false))
        assertFalse(AdEligibility.canDisplay(true, true, false))
        assertFalse(AdEligibility.canDisplay(true, false, true))
        assertFalse(AdEligibility.canDisplay(true, false, false, checking = true))
    }
}
