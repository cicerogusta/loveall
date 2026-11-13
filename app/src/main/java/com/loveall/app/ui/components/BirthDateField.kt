package com.loveall.app.ui.components

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import com.loveall.app.R
import com.loveall.app.domain.validation.Rules
import com.loveall.app.presentation.BirthDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

@Composable
fun BirthDateField(digits: String, onChange: (String) -> Unit, enabled: Boolean = true) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var touched by rememberSaveable { mutableStateOf(false) }
    var focusedOnce by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val invalid =
        digits.isNotEmpty() &&
            (digits.length == BirthDateFormat.MAX_DIGITS || touched) &&
            BirthDateFormat.toDate(digits) == null
    OutlinedTextField(
        value = digits,
        onValueChange = { onChange(BirthDateFormat.normalize(it)) },
        label = { Text(stringResource(R.string.birthdate)) },
        placeholder = { Text(stringResource(R.string.birthdate_placeholder)) },
        supportingText = {
            Text(
                stringResource(if (invalid) R.string.birthdate_invalid else R.string.birthdate_hint)
            )
        },
        enabled = enabled,
        isError = invalid,
        singleLine = true,
        modifier =
            Modifier.fillMaxWidth().onFocusChanged { focus ->
                if (focus.isFocused) focusedOnce = true else if (focusedOnce) touched = true
            },
        shape = MaterialTheme.shapes.small,
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        visualTransformation = BrazilianDateVisualTransformation,
        trailingIcon = {
            IconButton(
                onClick = {
                    keyboard?.hide()
                    showPicker = true
                },
                enabled = enabled,
            ) {
                Icon(Icons.Default.DateRange, stringResource(R.string.birthdate_choose))
            }
        },
    )
    if (showPicker) {
        val today = LocalDate.now()
        val initial =
            BirthDateFormat.toDate(digits)?.takeUnless { it.isAfter(today) }
                ?: today.minusYears(Rules.MIN_AGE.toLong())
        BrazilianDatePicker(
            initial,
            today,
            onSelect = {
                onChange(BirthDateFormat.fromDate(it))
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

object BrazilianDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = BirthDateFormat.display(digits)
        val mapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    (offset +
                            (if (
                                digits.length >= BirthDateFormat.DAY_END &&
                                    offset >= BirthDateFormat.DAY_END
                            )
                                1
                            else 0) +
                            (if (
                                digits.length >= BirthDateFormat.MONTH_END &&
                                    offset >= BirthDateFormat.MONTH_END
                            )
                                1
                            else 0))
                        .coerceIn(0, formatted.length)

                override fun transformedToOriginal(offset: Int): Int =
                    (offset -
                            (if (offset > BirthDateFormat.DAY_END) 1 else 0) -
                            (if (offset > BirthDateFormat.MONTH_END + 1) 1 else 0))
                        .coerceIn(0, digits.length)
            }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

@Composable
private fun BrazilianDatePicker(
    initial: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val title = stringResource(R.string.birthdate)
    val confirm = stringResource(R.string.confirm)
    val cancel = stringResource(R.string.cancel)
    DisposableEffect(context, configuration, initial, today) {
        // Keep the Activity as the base context so the localized dialog has a valid window token.
        val localized =
            ContextThemeWrapper(context, R.style.ThemeOverlay_LoveAll_DatePicker).apply {
                applyOverrideConfiguration(
                    Configuration(configuration).apply { setLocale(BirthDateFormat.locale) }
                )
            }
        val dialog =
            DatePickerDialog(
                localized,
                { _, year, month, day ->
                    currentOnSelect(LocalDate.of(year, month + 1, day))
                },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth,
            )
        dialog.setTitle(title)
        dialog.datePicker.maxDate =
            today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        dialog.datePicker.firstDayOfWeek =
            Calendar.getInstance(BirthDateFormat.locale).firstDayOfWeek
        dialog.setOnDismissListener { currentOnDismiss() }
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).text = confirm
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).text = cancel
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}
