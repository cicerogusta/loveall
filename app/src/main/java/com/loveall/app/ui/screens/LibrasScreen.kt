package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.platform.accessibility.LibrasLauncher
import com.loveall.app.ui.components.*
import com.loveall.app.ui.model.GuideVideoSource

@Composable
fun LibrasScreen(back: () -> Unit) {
    val context = LocalContext.current
    val launcher = remember(context) { LibrasLauncher(context) }
    var text by remember { mutableStateOf(EMPTY_TEXT) }
    var copied by remember { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    Page(stringResource(R.string.libras_title), back) {
        TutorialGuides()
        InfoCard(
            stringResource(R.string.libras_translator),
            stringResource(R.string.libras_translation_notice),
        )
        OutlinedTextField(
            value = text,
            onValueChange = {
                if (it.length <= MAX_TEXT_LENGTH) {
                    text = it
                    copied = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.libras_text_label)) },
            minLines = 3,
        )
        PrimaryButton(R.string.libras_copy, text.isNotBlank()) {
            launcher.copy(text)
            copied = true
        }
        if (copied) Text(stringResource(R.string.libras_copied))
        PrimaryButton(R.string.libras_open, true) { unavailable = !launcher.open() }
        if (unavailable)
            Text(
                stringResource(R.string.libras_unavailable),
                color = MaterialTheme.colorScheme.error,
            )
        LibrasVideoGuides()
    }
}

@Composable
private fun LibrasVideoGuides() {
    val topics =
        listOf(
            R.string.libras_guide_discovery to R.string.libras_video_discovery_url,
            R.string.libras_guide_safety to R.string.libras_video_safety_url,
            R.string.libras_guide_pcd to R.string.libras_video_pcd_url,
        )
    Text(stringResource(R.string.libras_guides), style = MaterialTheme.typography.headlineSmall)
    val hasVideos = topics.any { stringResource(it.second).isNotBlank() }
    if (!hasVideos) Text(stringResource(R.string.libras_guides_pending))
    topics.forEach { (titleId, urlId) ->
        val title = stringResource(titleId)
        val url = stringResource(urlId)
        if (url.isNotBlank()) GuideVideo(title, GuideVideoSource.Remote(url))
    }
}

private const val EMPTY_TEXT = ""
private const val MAX_TEXT_LENGTH = 2000
