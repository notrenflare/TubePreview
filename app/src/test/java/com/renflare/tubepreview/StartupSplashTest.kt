package com.renflare.tubepreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import com.renflare.tubepreview.ui.components.StartupSplash
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StartupSplashTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cachedLoadStillShowsVisibleIntroThenCompletesQuicklyOnce() {
        compose.mainClock.autoAdvance = false
        var completions = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                StartupSplash(isReady = true, onComplete = { completions++ })
            }
        }
        compose.mainClock.advanceTimeBy(250)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.onNodeWithContentDescription("Starting TubePreview").assertExists()
        compose.mainClock.advanceTimeBy(500)
        compose.runOnIdle { assertEquals(1, completions) }
        compose.mainClock.advanceTimeBy(2000)
        compose.runOnIdle { assertEquals(1, completions) }
    }

    @Test
    fun readinessDuringTheMorphKeepsTheIntroVisibleAndShortensTheFill() {
        compose.mainClock.autoAdvance = false
        val ready = mutableStateOf(false)
        var completions = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                StartupSplash(isReady = ready.value, onComplete = { completions++ })
            }
        }
        compose.mainClock.advanceTimeBy(200)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.runOnIdle { ready.value = true }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(100)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { assertEquals(1, completions) }
    }

    @Test
    fun delayedSettingsWaitThenFillAndSweepBeforeRevealingTheApp() {
        compose.mainClock.autoAdvance = false
        val ready = mutableStateOf(false)
        var completions = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                StartupSplash(isReady = ready.value, onComplete = { completions++ })
            }
        }
        compose.mainClock.advanceTimeBy(2000)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.onNodeWithContentDescription("Starting TubePreview").assertExists()
        compose.runOnIdle { ready.value = true }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(110)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.onNodeWithContentDescription("Starting TubePreview").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(1f, 0f..1f)
            )
        )
        compose.mainClock.advanceTimeBy(300)
        compose.runOnIdle { assertEquals(1, completions) }
    }
}
