package com.loveall.app.ui.model

import androidx.annotation.RawRes

sealed interface GuideVideoSource {
    data class Bundled(@param:RawRes val resource: Int) : GuideVideoSource

    data class Remote(val url: String) : GuideVideoSource
}
