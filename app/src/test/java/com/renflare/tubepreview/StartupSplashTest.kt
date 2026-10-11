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
import org.junit.Assert.assertTrue
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
    fun cachedLoadShowsTheFillAndConvergenceThenCompletesOnce() {
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
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { assertEquals(1, completions) }
        compose.mainClock.advanceTimeBy(2000)
        compose.runOnIdle { assertEquals(1, completions) }
    }

    @Test
    fun readinessDuringMorphDoesNotSkipTheVisibleFill() {
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
        compose.mainClock.advanceTimeBy(850)
        compose.runOnIdle { assertEquals(1, completions) }
    }

    @Test
    fun cachedLaunchShowsIntermediateFillInsteadOfFlashingAFullBar() {
        compose.mainClock.autoAdvance = false
        var completions = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                StartupSplash(isReady = true, onComplete = { completions++ })
            }
        }
        fun progress() = compose.onNodeWithContentDescription("Starting TubePreview")
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        compose.mainClock.advanceTimeBy(480)
        val early = progress()
        assertTrue("A cached load still shows a partially filled line: $early", early > 0f && early < 0.8f)
        compose.mainClock.advanceTimeBy(170)
        val late = progress()
        assertTrue("Fill must visibly advance before exit: $early -> $late", late > early && late < 1f)
        compose.runOnIdle { assertEquals(0, completions) }
        compose.mainClock.advanceTimeBy(500)
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
        compose.onNodeWithContentDescription("Starting TubePreview").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0.92f, 0f..1f)
            )
        )
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
