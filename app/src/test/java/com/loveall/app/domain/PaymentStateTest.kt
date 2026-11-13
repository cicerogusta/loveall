package com.loveall.app.domain

import com.loveall.app.domain.model.PaymentState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class PaymentStateTest(
    private val wire: String?,
    private val expected: PaymentState,
    private val terminal: Boolean,
) {
    @Test
    fun `given backend status when mapped then state and finality match contract`() {
        // Act
        val actual = PaymentState.fromWire(wire)

        // Assert
        assertEquals(expected, actual)
        assertEquals(terminal, actual.isTerminal)
    }

    companion object {
        private const val CASE_NAME = "wire={0}, state={1}, terminal={2}"

        @JvmStatic
        @Parameterized.Parameters(name = CASE_NAME)
        fun backendStatusCases(): List<Array<Any?>> =
            listOf(
                arrayOf("pending", PaymentState.PENDING, false),
                arrayOf("in_process", PaymentState.IN_PROCESS, false),
                arrayOf("approved", PaymentState.APPROVED, true),
                arrayOf("rejected", PaymentState.REJECTED, true),
                arrayOf("failed", PaymentState.FAILED, true),
                arrayOf("cancelled", PaymentState.CANCELLED, true),
                arrayOf("refunded", PaymentState.REFUNDED, true),
                arrayOf("charged_back", PaymentState.CHARGED_BACK, true),
                arrayOf("not_found", PaymentState.NOT_FOUND, true),
                arrayOf("unknown", PaymentState.UNKNOWN, false),
                arrayOf("future_backend_status", PaymentState.UNKNOWN, false),
                arrayOf(null, PaymentState.UNKNOWN, false),
            )
    }
}
