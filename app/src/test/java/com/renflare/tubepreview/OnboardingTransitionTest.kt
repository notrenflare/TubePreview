package com.renflare.tubepreview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.data.AvatarCrop
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.screens.*
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h852dp-xxhdpi")
class OnboardingTransitionTest {
    @get:Rule val compose = createComposeRule()
    private val settings = CreatorSettings("renflare", "", "", true, 15, "", "", "", "", 5, false, "", "", "")

    private fun capture(tag: String): Bitmap {
        val file = File.createTempFile("tour-frame", ".png")
        try {
            compose.onNodeWithTag(tag).captureRoboImage(file.absolutePath,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
            return BitmapFactory.decodeFile(file.absolutePath)
        } finally {
            file.delete()
        }
    }

    private fun contrast(tag: String, light: Boolean = false): Double {
        val bitmap = capture(tag)
        try {
            var sum = 0.0
            for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val level = android.graphics.Color.red(pixel) +
                    android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)
                sum += if (light) 765 - level else level
            }
            return sum / (bitmap.width * bitmap.height)
        } finally {
            bitmap.recycle()
        }
    }

    @Test fun startLabelIsCompleteOnTheFrameThatTheLastSlideArrives() {
        lateinit var pager: PagerState
        lateinit var scope: CoroutineScope
        compose.setContent {
            TubePreviewTheme {
                pager = rememberPagerState(initialPage = 2, pageCount = { 4 })
                scope = rememberCoroutineScope()
                Column(Modifier.fillMaxSize()) {
                    TourHeaderAction(pager, false, {}, {})
                    HorizontalPager(pager, modifier = Modifier.weight(1f)) { Box(Modifier.fillMaxSize()) }
                    TourNavigation(pager, false, false, false, true, {}, {})
                }
            }
        }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { scope.launch { pager.animateScrollToPage(3, animationSpec = tween(360)) } }
        compose.mainClock.advanceTimeUntil(1000) { pager.settledPage == 3 && !pager.isScrollInProgress }
        compose.onNodeWithText("Start previewing").assertExists()
        val arrival = capture("onboarding_primary_action")
        val headerArrival = capture("onboarding_header_action")
        compose.onNodeWithText("Use defaults").assertExists()
        compose.mainClock.advanceTimeBy(200)
        val later = capture("onboarding_primary_action")
        val headerLater = capture("onboarding_header_action")
        try {
            assertTrue("The label must not keep fading after the slide arrives", arrival.sameAs(later))
            assertTrue("The header is also complete on arrival", headerArrival.sameAs(headerLater))
        } finally {
            arrival.recycle()
            later.recycle()
            headerArrival.recycle()
            headerLater.recycle()
        }
    }

    @Test fun keyboardOpeningAndClosingRetraceTheSameLayoutAndPreserveFocus() {
        val progress = mutableFloatStateOf(0f)
        val name = mutableStateOf("renflare")
        val dark = mutableStateOf(true)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                Box(Modifier.fillMaxWidth().height((600 - 320 * progress.floatValue).dp)) {
                    TourPage(3, false, progress.floatValue > 0f, name.value, { name.value = it },
                        "", AvatarCrop(), {}, keyboardProgress = progress.floatValue)
                }
            }
        }
        compose.onNodeWithTag("onboarding_name_input").performClick().assertIsFocused()
        compose.onNodeWithTag("onboarding_name_input").performTextReplacement("My studio")
        for (theme in listOf(true, false)) {
            compose.runOnIdle { dark.value = theme }
            val heights = mutableMapOf<Float, Float>()
            for (p in listOf(0f, 0.25f, 0.5f, 0.75f, 0.99f)) {
                compose.runOnIdle { progress.floatValue = p }
                val bounds = compose.onNodeWithTag("onboarding_copy_3").getUnclippedBoundsInRoot()
                heights[p] = (bounds.bottom - bounds.top).value
                compose.onNodeWithTag("onboarding_name_input").assertIsFocused().assertTextContains("My studio")
            }
            compose.runOnIdle { progress.floatValue = 1f }
            compose.onNodeWithTag("onboarding_heading_3").assertDoesNotExist()
            compose.onNodeWithContentDescription("Choose channel photo").assertIsDisplayed()
            for (p in listOf(0.99f, 0.75f, 0.5f, 0.25f, 0.01f, 0f)) {
                compose.runOnIdle { progress.floatValue = p }
                val bounds = compose.onNodeWithTag("onboarding_copy_3").getUnclippedBoundsInRoot()
                val height = (bounds.bottom - bounds.top).value
                if (p != 0.01f) assertEquals("Reversing the IME retraces its measured space", heights.getValue(p), height, 1f)
                else assertTrue("The final dismissal frame has no large jump", heights.getValue(0f) - height < 3f)
                compose.onNodeWithTag("onboarding_name_input").assertIsFocused().assertTextContains("My studio")
            }
        }
    }

    @Test fun entrancesWaitForTheSplashAndDoNotReplayOnRevisitsOrThemeChanges() {
        val ready = mutableStateOf(false)
        val dark = mutableStateOf(true)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                OnboardingScreen(settings.copy(isDarkMode = dark.value), { _, _ -> }, {}, {},
                    animationsReady = ready.value)
            }
        }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(1000)
        assertEquals("Entrance waits until the launch overlay finishes", 0.0, contrast("onboarding_heading_0"), 0.01)
        compose.runOnIdle { ready.value = true }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(80)
        compose.waitForIdle()
        val early = contrast("onboarding_heading_0")
        compose.mainClock.advanceTimeBy(240)
        compose.waitForIdle()
        val middle = contrast("onboarding_heading_0")
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val complete = contrast("onboarding_heading_0")
        assertTrue("The entrance progresses gradually", early > 0 && middle > early * 1.2)
        assertTrue("The entrance remains visible in motion instead of finishing in a flash",
            complete > middle * 1.05)
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.mainClock.advanceTimeBy(400)
        assertEquals("Returning to a visited slide does not replay its entrance", complete, contrast("onboarding_heading_0"), 0.1)
        compose.runOnIdle { dark.value = false }
        assertTrue("Theme changes keep the heading fully visible", contrast("onboarding_heading_0", light = true) > complete * 0.8)
    }
}
