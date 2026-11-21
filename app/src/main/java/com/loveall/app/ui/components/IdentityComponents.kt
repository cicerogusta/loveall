package com.loveall.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.loveall.app.R
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.ui.theme.*

@Composable
fun GenderPicker(
    selected: List<String>,
    multiple: Boolean,
    onChange: (List<String>) -> Unit,
    enabled: Boolean = true,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Gender.entries.forEach { gender ->
            val checked = gender.wire in selected
            FilterChip(
                selected = checked,
                enabled = enabled,
                onClick = {
                    onChange(
                        if (!multiple) listOf(gender.wire)
                        else if (checked) selected - gender.wire else selected + gender.wire
                    )
                },
                label = { Text(stringResource(gender.label())) },
                leadingIcon =
                    if (checked)
                        ({
                            Icon(Icons.Default.Check, null, Modifier.size(AppDimensions.smallIcon))
                        })
                    else null,
                shape = CircleShape,
                modifier = Modifier.heightIn(min = AppDimensions.touchTarget),
            )
        }
    }
}

@StringRes
fun Gender.label() =
    when (this) {
        Gender.WOMAN -> R.string.gender_woman
        Gender.MAN -> R.string.gender_man
        Gender.NON_BINARY -> R.string.gender_non_binary
        Gender.TRANS_WOMAN -> R.string.gender_trans_woman
        Gender.TRANS_MAN -> R.string.gender_trans_man
        Gender.GENDERQUEER -> R.string.gender_queer
        Gender.GENDERFLUID -> R.string.gender_fluid
        Gender.AGENDER -> R.string.gender_agender
        Gender.BIGENDER -> R.string.gender_bigender
        Gender.TWO_SPIRIT -> R.string.gender_two_spirit
        Gender.OTHER -> R.string.gender_other
        Gender.PREFER_NOT_SAY -> R.string.gender_private
    }

@StringRes
fun ReportReason.label() =
    when (this) {
        ReportReason.FAKE -> R.string.reason_fake
        ReportReason.CONTENT -> R.string.reason_content
        ReportReason.HARASSMENT -> R.string.reason_harassment
        ReportReason.SPAM -> R.string.reason_spam
        ReportReason.UNDERAGE -> R.string.reason_underage
        ReportReason.SCAM -> R.string.reason_scam
        ReportReason.HATE -> R.string.reason_hate
        ReportReason.VIOLENCE -> R.string.reason_violence
        ReportReason.OTHER -> R.string.gender_other
    }
