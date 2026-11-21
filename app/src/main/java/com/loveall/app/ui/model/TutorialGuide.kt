package com.loveall.app.ui.model

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.loveall.app.R

enum class TutorialGuide(
    @param:StringRes val title: Int,
    @param:RawRes val video: Int,
    @param:StringRes val transcript: Int,
) {
    DISCOVERY(
        R.string.libras_guide_discovery,
        R.raw.tutorial_discovery,
        R.string.tutorial_discovery_transcript,
    ),
    SAFETY(
        R.string.libras_guide_safety,
        R.raw.tutorial_safety,
        R.string.tutorial_safety_transcript,
    ),
    PCD(R.string.libras_guide_pcd, R.raw.tutorial_pcd, R.string.tutorial_pcd_transcript),
}
