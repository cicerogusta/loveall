package com.loveall.app.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.loveall.app.R
import com.loveall.app.ui.model.GuideVideoSource
import com.loveall.app.ui.theme.AppSpacing

/** Reproduz arquivos incluídos no app ou HTTPS revisado, sem WebView nem tokens da API. */
@Composable
fun GuideVideo(title: String, source: GuideVideoSource) {
    key(source) { GuideVideoPlayer(title, source) }
}

@Composable
private fun GuideVideoPlayer(title: String, source: GuideVideoSource) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val owner = LocalLifecycleOwner.current
    var video by remember { mutableStateOf<VideoView?>(null) }
    var ready by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val uri =
        remember(source) {
            when (source) {
                is GuideVideoSource.Bundled ->
                    Uri.Builder()
                        .scheme(ANDROID_RESOURCE)
                        .authority(context.packageName)
                        .appendPath(source.resource.toString())
                        .build()
                is GuideVideoSource.Remote -> source.url.toUri()
            }
        }
    val valid =
        when (source) {
            is GuideVideoSource.Bundled -> source.resource > 0
            is GuideVideoSource.Remote ->
                uri.scheme == HTTPS && !uri.host.isNullOrBlank() && uri.userInfo == null
        }
    val aspect = if (source is GuideVideoSource.Bundled) 9f / 16f else 16f / 9f
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (valid && !failed) {
            TextButton(onClick = { video?.start() }, enabled = ready) {
                Text(stringResource(R.string.guide_video_play))
            }
            AndroidView(
                modifier = Modifier.fillMaxWidth().aspectRatio(aspect),
                factory = { context ->
                    VideoView(context).apply {
                        id = R.id.guide_video_view
                        contentDescription = title
                        setMediaController(MediaController(context).also { it.setAnchorView(this) })
                        setOnPreparedListener { ready = true }
                        setOnErrorListener { _, _, _ ->
                            failed = true
                            true
                        }
                        setVideoURI(uri)
                        video = this
                    }
                },
                onRelease = {
                    it.stopPlayback()
                    if (video === it) video = null
                },
            )
        } else
            Text(
                stringResource(R.string.libras_video_error),
                color = MaterialTheme.colorScheme.error,
            )
    }
    DisposableEffect(owner, video) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) video?.pause()
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            video?.pause()
        }
    }
}

private const val HTTPS = "https"
private const val ANDROID_RESOURCE = "android.resource"
