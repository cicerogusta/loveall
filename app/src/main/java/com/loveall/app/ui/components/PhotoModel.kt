package com.loveall.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.loveall.app.BuildConfig
import com.loveall.app.domain.validation.Rules
import java.net.URI

val LocalPhotoBaseUrl = staticCompositionLocalOf { BuildConfig.API_BASE_URL }

@Composable
fun photoModel(reference: String): String =
    if (Rules.uploadedPhoto(reference)) {
        URI(LocalPhotoBaseUrl.current).resolve(reference).toString()
    } else reference
