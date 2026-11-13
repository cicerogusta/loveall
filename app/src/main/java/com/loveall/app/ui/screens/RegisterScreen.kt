package com.loveall.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.loveall.app.R
import com.loveall.app.core.text.TextDefaults
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.Registration
import com.loveall.app.presentation.BirthDateFormat
import com.loveall.app.presentation.state.SessionState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun RegisterScreen(state: SessionState, onBack: () -> Unit, onRegister: (Registration) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(TextDefaults.EMPTY) }
    var email by rememberSaveable { mutableStateOf(TextDefaults.EMPTY) }
    var password by remember { mutableStateOf(TextDefaults.EMPTY) }
    var birthdate by rememberSaveable { mutableStateOf(TextDefaults.EMPTY) }
    var gender by rememberSaveable { mutableStateOf(Gender.PREFER_NOT_SAY.wire) }
    var custom by rememberSaveable { mutableStateOf(TextDefaults.EMPTY) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var lookingFor by rememberSaveable { mutableStateOf(emptyList<String>()) }
    BackHandler(enabled = step > 0 && !state.busy) { step-- }
    Page(stringResource(R.string.register_title), { if (step > 0) step-- else onBack() }) {
        Eyebrow(stringResource(R.string.register_step, step + 1))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            repeat(REGISTRATION_STEPS) { index ->
                Surface(
                    Modifier.weight(1f).height(AppSpacing.tiny),
                    shape = CircleShape,
                    color =
                        if (index <= step) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                ) {}
            }
        }
        ErrorCard(state.error)
        when (step) {
            0 -> {
                SectionTitle(
                    stringResource(R.string.register_basics_title),
                    stringResource(R.string.register_basics_body),
                )
                AppField(name, { name = it }, R.string.name, !state.busy)
                AppField(
                    email,
                    { email = it },
                    R.string.email,
                    !state.busy,
                    keyboardType = KeyboardType.Email,
                )
                PasswordField(password, { password = it }, !state.busy)
                Text(
                    stringResource(R.string.register_rules),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrimaryButton(
                    R.string.continue_action,
                    !state.busy && name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                ) {
                    step++
                }
            }
            1 -> {
                Text(
                    stringResource(R.string.register_identity_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                BirthDateField(birthdate, { birthdate = it }, !state.busy)
                FormSection(stringResource(R.string.identity)) {
                    GenderPicker(listOf(gender), false, { gender = it.first() }, !state.busy)
                    if (gender == Gender.OTHER.wire)
                        AppField(custom, { custom = it }, R.string.custom_identity, !state.busy)
                }
                PrimaryButton(
                    R.string.continue_action,
                    !state.busy && BirthDateFormat.toDate(birthdate) != null,
                ) {
                    step++
                }
            }
            else -> {
                Text(
                    stringResource(R.string.register_interests_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                FormSection(
                    stringResource(R.string.looking_for),
                    stringResource(R.string.looking_for_hint),
                ) {
                    GenderPicker(lookingFor, true, { lookingFor = it }, !state.busy)
                }
                LegalDocuments()
                Row {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        enabled = !state.busy,
                    )
                    Text(stringResource(R.string.accept_terms))
                }
                PrimaryButton(R.string.create_account, !state.busy && termsAccepted) {
                    onRegister(
                        Registration(
                            name,
                            email,
                            password,
                            BirthDateFormat.toIso(birthdate).orEmpty(),
                            gender,
                            custom,
                            lookingFor,
                            termsAccepted,
                        )
                    )
                }
            }
        }
    }
}

private const val REGISTRATION_STEPS = 3
