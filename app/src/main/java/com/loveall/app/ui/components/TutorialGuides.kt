package com.loveall.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.model.GuideVideoSource
import com.loveall.app.ui.model.TutorialGuide

@Composable
fun TutorialGuides() {
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    var showTranscript by rememberSaveable(selected) { mutableStateOf(false) }
    InfoCard(stringResource(R.string.tutorials_title), stringResource(R.string.tutorials_notice))
    TutorialGuide.entries.forEach { guide ->
        OutlinedButton(onClick = { selected = guide.ordinal }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(guide.title))
        }
    }
    selected
        ?.let { TutorialGuide.entries.getOrNull(it) }
        ?.let { guide ->
            GuideVideo(stringResource(guide.title), GuideVideoSource.Bundled(guide.video))
            TextButton(onClick = { showTranscript = !showTranscript }) {
                Text(
                    stringResource(
                        if (showTranscript) R.string.tutorial_hide_text
                        else R.string.tutorial_show_text
                    )
                )
            }
            if (showTranscript) Text(stringResource(guide.transcript))
        }
}
