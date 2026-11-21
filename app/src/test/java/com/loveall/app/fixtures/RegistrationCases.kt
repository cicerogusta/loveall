package com.loveall.app.fixtures

import com.loveall.app.TestFixtures
import com.loveall.app.domain.model.Gender

internal object RegistrationCases {
    private val valid = TestFixtures.registration
    val all =
        listOf(
            RegistrationCase(
                "bcrypt ASCII byte boundary",
                valid.copy(
                    password = TestInputs.FILLER.repeat(71) + TestInputs.DIGITS_PASSWORD.take(1)
                ),
                true,
            ),
            RegistrationCase(
                "bcrypt suffix would be truncated",
                valid.copy(
                    password = TestInputs.FILLER.repeat(72) + TestInputs.DIGITS_PASSWORD.take(1)
                ),
                false,
            ),
            RegistrationCase(
                "bcrypt Unicode byte boundary exceeded",
                valid.copy(password = TestInputs.MULTIBYTE_PASSWORD),
                false,
            ),
            RegistrationCase(
                "password above API limit",
                valid.copy(
                    password = TestInputs.LETTERS_PASSWORD.repeat(12) + TestInputs.DIGITS_PASSWORD
                ),
                false,
            ),
            RegistrationCase(
                "custom identity above API limit",
                valid.copy(genderCustom = TestInputs.FILLER.repeat(101)),
                false,
            ),
            RegistrationCase(
                "duplicate preferences",
                valid.copy(lookingFor = listOf(Gender.MAN.wire, Gender.MAN.wire)),
                false,
            ),
            RegistrationCase("terms not accepted", valid.copy(termsAccepted = false), false),
            RegistrationCase(
                "adult with identity preferences",
                valid.copy(lookingFor = listOf(TestUsers.WOMAN_WIRE, Gender.TRANS_MAN.wire)),
                true,
            ),
            RegistrationCase(
                "eighteenth birthday",
                valid.copy(birthdate = TestBirthDates.EIGHTEENTH_BIRTHDAY),
                true,
            ),
            RegistrationCase(
                "underage by one day",
                valid.copy(birthdate = TestBirthDates.TURNS_EIGHTEEN_TOMORROW),
                false,
            ),
            RegistrationCase("future date", valid.copy(birthdate = TestBirthDates.FUTURE), false),
            RegistrationCase(
                "impossible date",
                valid.copy(birthdate = TestBirthDates.IMPOSSIBLE),
                false,
            ),
            RegistrationCase(
                "age above limit",
                valid.copy(birthdate = TestBirthDates.TOO_OLD),
                false,
            ),
            RegistrationCase("missing date", valid.copy(birthdate = TestInputs.EMPTY), false),
            RegistrationCase(
                "localized date at domain boundary",
                valid.copy(birthdate = TestBirthDates.BRAZILIAN_INPUT),
                false,
            ),
            RegistrationCase("name too short", valid.copy(name = TestInputs.ONE_LETTER), false),
            RegistrationCase("number in name", valid.copy(name = TestInputs.NUMBERED_NAME), false),
            RegistrationCase(
                "Roman numeral in name",
                valid.copy(name = TestInputs.ROMAN_NUMBERED_NAME),
                false,
            ),
            RegistrationCase(
                "supplementary numeric code point in name",
                valid.copy(name = TestInputs.SUPPLEMENTARY_NUMBERED_NAME),
                false,
            ),
            RegistrationCase(
                "name above limit",
                valid.copy(name = TestInputs.ONE_LETTER.repeat(51)),
                false,
            ),
            RegistrationCase(
                "malformed email",
                valid.copy(email = TestInputs.INVALID_EMAIL),
                false,
            ),
            RegistrationCase(
                "password without digits",
                valid.copy(password = TestInputs.LETTERS_PASSWORD),
                false,
            ),
            RegistrationCase(
                "password without letters",
                valid.copy(password = TestInputs.DIGITS_PASSWORD),
                false,
            ),
            RegistrationCase(
                "password too short",
                valid.copy(password = TestInputs.SHORT_PASSWORD),
                false,
            ),
            RegistrationCase("missing identity", valid.copy(gender = TestInputs.EMPTY), false),
            RegistrationCase(
                "other identity without description",
                valid.copy(gender = Gender.OTHER.wire),
                false,
            ),
            RegistrationCase(
                "other identity described",
                valid.copy(gender = Gender.OTHER.wire, genderCustom = TestUsers.CUSTOM_IDENTITY),
                true,
            ),
            RegistrationCase(
                "unknown preference",
                valid.copy(lookingFor = listOf(TestInputs.INVALID_PREFERENCE)),
                false,
            ),
        )
}
