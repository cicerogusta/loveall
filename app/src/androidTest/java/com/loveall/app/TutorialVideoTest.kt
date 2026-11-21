package com.loveall.app

import android.media.MediaMetadataRetriever
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.ui.components.Page
import com.loveall.app.ui.components.TutorialGuides
import com.loveall.app.ui.model.TutorialGuide
import com.loveall.app.ui.theme.LoveAllTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TutorialVideoTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun bundledTutorialsDecodeAsPortraitVideoWithExpectedDuration() {
        val seconds =
            mapOf(
                TutorialGuide.DISCOVERY to 46,
                TutorialGuide.SAFETY to 49,
                TutorialGuide.PCD to 50,
            )
        seconds.forEach { (guide, duration) ->
            context.resources.openRawResourceFd(guide.video).use { file ->
                MediaMetadataRetriever().use { metadata ->
                    metadata.setDataSource(file.fileDescriptor, file.startOffset, file.length)
                    assertEquals(
                        duration * 1000L,
                        metadata
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLong(),
                    )
                    assertEquals(
                        "720",
                        metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                    )
                    assertEquals(
                        "1280",
                        metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
                    )
                    assertNotEquals(
                        "yes",
                        metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO),
                    )
                    val frame = metadata.getFrameAtTime(duration * 500_000L)
                    assertNotNull(frame)
                    frame?.recycle()
                }
            }
        }
    }

    @Test
    fun allTutorialsPlayOfflineAndExposeScalableTranscript() {
        showGuides()
        TutorialGuide.entries.forEach { guide ->
            choose(guide)
            compose
                .onNodeWithText(context.getString(R.string.guide_video_play))
                .performScrollTo()
                .performClick()
            compose.runOnIdle { assertTrue(video().isPlaying) }
            compose
                .onNodeWithText(context.getString(R.string.tutorial_show_text))
                .performScrollTo()
                .performClick()
            compose.onNodeWithText(context.getString(guide.transcript)).assertExists()
            compose
                .onNodeWithText(context.getString(R.string.tutorial_hide_text))
                .performScrollTo()
                .performClick()
            compose.onNodeWithText(context.getString(guide.transcript)).assertDoesNotExist()
        }
    }

    @Test
    fun playbackPausesWhenActivityLeavesForeground() {
        showGuides()
        choose(TutorialGuide.DISCOVERY)
        compose
            .onNodeWithText(context.getString(R.string.guide_video_play))
            .performScrollTo()
            .performClick()
        lateinit var playing: VideoView
        compose.runOnIdle {
            playing = video()
            assertTrue(playing.isPlaying)
        }
        compose.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertFalse(playing.isPlaying)
        }
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
    }

    private fun showGuides() {
        compose.setContent {
            LoveAllTheme { Page(context.getString(R.string.tutorials_title)) { TutorialGuides() } }
        }
    }

    private fun choose(guide: TutorialGuide) {
        compose
            .onNode(hasText(context.getString(guide.title)) and hasClickAction())
            .performScrollTo()
            .performClick()
        compose.waitUntil(10_000) {
            var prepared = false
            compose.runOnIdle {
                prepared =
                    compose.activity.findViewById<VideoView>(R.id.guide_video_view)?.duration?.let {
                        it > 0
                    } == true
            }
            prepared
        }
        compose.onNodeWithText(context.getString(R.string.guide_video_play)).assertIsEnabled()
    }

    private fun video(): VideoView =
        requireNotNull(compose.activity.findViewById(R.id.guide_video_view))
}
