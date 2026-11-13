package com.loveall.app.domain

import com.loveall.app.domain.validation.AdConsentGate
import org.junit.Assert.*
import org.junit.Test

class AdConsentGateTest {
    @Test
    fun `advertising needs both current permission and completed SDK initialization`() {
        val gate = AdConsentGate()
        assertFalse(gate.canInitialize)
        gate.confirm(true)
        assertTrue(gate.canInitialize)
        assertFalse(gate.canLoadAds)
        gate.sdkInitialized()
        assertTrue(gate.canLoadAds)
    }

    @Test
    fun `late SDK callback cannot override a failed consent refresh`() {
        val gate = AdConsentGate()
        gate.confirm(true)
        gate.beginUpdate()
        gate.fail()
        gate.sdkInitialized()
        assertFalse(gate.canInitialize)
        assertFalse(gate.canLoadAds)
    }

    @Test
    fun `revocation immediately blocks an already initialized SDK`() {
        val gate = AdConsentGate()
        gate.confirm(true)
        gate.sdkInitialized()
        gate.beginUpdate()
        assertFalse(gate.canLoadAds)
        gate.confirm(false)
        assertFalse(gate.canLoadAds)
    }
}
