package com.loveall.app.fixtures

import com.loveall.app.TestFixtures
import com.loveall.app.domain.model.Gender

internal object ProfileEditCases {
    private val valid = TestFixtures.edit
    val all =
        listOf(
            ProfileEditCase(
                "city above API limit",
                valid.copy(city = TestInputs.FILLER.repeat(101)),
                false,
            ),
            ProfileEditCase(
                "custom identity above API limit",
                valid.copy(genderCustom = TestInputs.FILLER.repeat(101)),
                false,
            ),
            ProfileEditCase(
                "duplicate preferences",
                valid.copy(lookingFor = listOf(Gender.MAN.wire, Gender.MAN.wire)),
                false,
            ),
            ProfileEditCase(
                "duplicate photos",
                valid.copy(photos = List(2) { TestPhotos.VALID_URL }),
                false,
            ),
            ProfileEditCase("valid profile", valid, true),
            ProfileEditCase(
                "inclusive upper bounds",
                valid.copy(
                    ageMin = 18,
                    ageMax = 99,
                    maxDistance = 200,
                    photos = List(6) { TestPhotos.VALID_URL + it },
                    bio = TestInputs.FILLER.repeat(500),
                ),
                true,
            ),
            ProfileEditCase("missing name", valid.copy(name = TestInputs.EMPTY), false),
            ProfileEditCase(
                "biography above limit",
                valid.copy(bio = TestInputs.FILLER.repeat(501)),
                false,
            ),
            ProfileEditCase("age below limit", valid.copy(ageMin = 17), false),
            ProfileEditCase("age above limit", valid.copy(ageMax = 100), false),
            ProfileEditCase("inverted age interval", valid.copy(ageMin = 40, ageMax = 30), false),
            ProfileEditCase("distance below limit", valid.copy(maxDistance = 4), false),
            ProfileEditCase("distance above limit", valid.copy(maxDistance = 201), false),
            ProfileEditCase("missing identity", valid.copy(gender = TestInputs.EMPTY), false),
            ProfileEditCase(
                "other identity without description",
                valid.copy(gender = Gender.OTHER.wire),
                false,
            ),
            ProfileEditCase(
                "unknown identity",
                valid.copy(gender = TestInputs.UNKNOWN_IDENTITY),
                false,
            ),
            ProfileEditCase(
                "unknown preference",
                valid.copy(lookingFor = listOf(TestInputs.UNKNOWN_IDENTITY)),
                false,
            ),
            ProfileEditCase(
                "too many photos",
                valid.copy(photos = List(7) { TestPhotos.VALID_URL }),
                false,
            ),
            ProfileEditCase(
                "insecure photo reference",
                valid.copy(photos = listOf(TestInputs.INSECURE_PHOTO_URL)),
                false,
            ),
            ProfileEditCase(
                "other identity described",
                valid.copy(gender = Gender.OTHER.wire, genderCustom = TestUsers.CUSTOM_IDENTITY),
                true,
            ),
        )
}
