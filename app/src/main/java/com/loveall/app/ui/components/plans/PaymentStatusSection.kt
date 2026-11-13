package com.loveall.app.ui.components.plans

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.PaymentState
import com.loveall.app.ui.components.InfoCard
import com.loveall.app.ui.components.PrimaryButton

@Composable
internal fun PaymentStatusSection(
    status: PaymentState?,
    pending: Boolean,
    enabled: Boolean,
    onCheckPayment: () -> Unit,
) {
    InfoCard(
        stringResource(R.string.payment_title),
        stringResource(status.paymentLabel()),
    ) {
        if (pending)
            PrimaryButton(
                R.string.check_payment,
                enabled,
                onCheckPayment,
            )
    }
}

private fun PaymentState?.paymentLabel() =
    when (this) {
        null,
        PaymentState.PENDING,
        PaymentState.IN_PROCESS -> R.string.payment_pending
        PaymentState.APPROVED -> R.string.payment_approved
        PaymentState.REJECTED,
        PaymentState.FAILED,
        PaymentState.CANCELLED,
        PaymentState.REFUNDED,
        PaymentState.CHARGED_BACK -> R.string.payment_failed
        PaymentState.NOT_FOUND,
        PaymentState.UNKNOWN -> R.string.payment_unknown
    }
