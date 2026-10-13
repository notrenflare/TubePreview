package com.renflare.tubepreview

import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.screens.OnboardingScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import java.io.File
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
class OnboardingMotionTest {
    @get:Rule val compose = createComposeRule()
    private val settings = CreatorSettings("renflare", "", "", true, 15, "", "", "", "", 5, false, "", "", "")

    private fun indicatorWidth(): Int {
        val file = File.createTempFile("tour-indicator", ".png")
        try {
            compose.onNodeWithTag("onboarding_indicator").captureRoboImage(file.absolutePath,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val width = (0 until bitmap.width).count { x ->
                val color = bitmap.getPixel(x, bitmap.height / 2)
                android.graphics.Color.red(color) > 150 &&
                    android.graphics.Color.red(color) > android.graphics.Color.green(color) * 2
            }
            bitmap.recycle()
            return width
        } finally {
            file.delete()
        }
    }

    @Test fun heldAndReversedSwipesStretchIndicatorAndResizeButtonContinuouslyInBothThemes() {
        val dark = mutableStateOf(true)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                OnboardingScreen(settings.copy(isDarkMode = dark.value), { _, _ -> }, {}, {})
            }
        }
        for (theme in listOf(true, false)) {
            compose.runOnIdle { dark.value = theme }
            compose.mainClock.autoAdvance = false
            val firstWidth = compose.onNodeWithTag("onboarding_next").getUnclippedBoundsInRoot().let { (it.right - it.left).value }
            val initialIndicator = indicatorWidth()
            assertTrue("The resting active indicator is an 8dp dot", initialIndicator in 20..28)
            val indicatorBounds = compose.onNodeWithTag("onboarding_indicator").getUnclippedBoundsInRoot()
            assertEquals("The four steps form a compact group", 96f,
                (indicatorBounds.right - indicatorBounds.left).value, 1f)
            compose.onNodeWithTag("onboarding_pager").performTouchInput {
                down(Offset(width * 0.85f, height / 2f))
                moveTo(Offset(width * 0.5f, height / 2f), delayMillis = 300)
            }
            compose.mainClock.advanceTimeBy(32)
            compose.onNodeWithTag("onboarding_next").assertIsEnabled()
            val intermediateWidth = compose.onNodeWithTag("onboarding_next").getUnclippedBoundsInRoot().let { (it.right - it.left).value }
            assertTrue("Back reveals space gradually", intermediateWidth < firstWidth - 5f && intermediateWidth > firstWidth - 55f)
            assertTrue("The active pill stretches between dots", indicatorWidth() > initialIndicator * 1.5f)
            compose.onNodeWithTag("onboarding_pager").performTouchInput {
                moveTo(Offset(width * 0.85f, height / 2f), delayMillis = 300)
                up()
            }
            compose.mainClock.advanceTimeBy(1000)
            compose.mainClock.autoAdvance = true
            compose.waitForIdle()
            compose.onNodeWithTag("onboarding_page_0").assertIsDisplayed()
            assertEquals(firstWidth, compose.onNodeWithTag("onboarding_next").getUnclippedBoundsInRoot().let { (it.right - it.left).value }, 1f)
            assertEquals(initialIndicator.toDouble(), indicatorWidth().toDouble(), 3.0)
        }
    }

    @Test fun repeatedTapsDuringButtonTransitionDoNotQueueExtraSlides() {
        var completions = 0
        compose.setContent {
            TubePreviewTheme {
                OnboardingScreen(settings, { _, _ -> }, { completions++ }, {})
            }
        }
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.mainClock.advanceTimeBy(32)
        repeat(15) { compose.onNodeWithTag("onboarding_next").assertIsEnabled().performClick() }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithTag("onboarding_page_1").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_page_2").assertIsNotDisplayed()
        assertEquals(0, completions)
    }
}
