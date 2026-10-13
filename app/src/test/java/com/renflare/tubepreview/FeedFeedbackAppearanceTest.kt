package com.renflare.tubepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.ui.components.*
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Bitmap capture uses native graphics on API 29+; interaction tests also cover API 24.
@Config(sdk = [29, 36], qualifiers = "w393dp-h852dp-xxhdpi")
class FeedFeedbackAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun feedbackRemainsReadableAndDismissibleInBothThemes() {
        val dark = mutableStateOf(true)
        lateinit var state: FeedFeedbackState
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                state = rememberFeedFeedbackState()
                val backdrop = rememberFeedBackdrop()
                val list = rememberLazyListState()
                Box(Modifier.fillMaxSize().testTag("feedback_scene")) {
                    Column(Modifier.fillMaxSize().feedBackdropSource(backdrop, list)) {
                        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF42776A)))
                        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFC47D52)))
                    }
                    Box(Modifier.align(Alignment.BottomCenter)) {
                        FeedFeedbackHost(state, backdrop, dark.value)
                    }
                }
            }
        }
        compose.runOnIdle { state.show("Demo video · A weekend worth remembering") }
        for (theme in listOf(true, false)) {
            compose.runOnIdle { dark.value = theme }
            compose.onNodeWithText("Demo video · A weekend worth remembering").assertIsDisplayed()
            compose.onNodeWithContentDescription("Dismiss").assertIsDisplayed().assertHasClickAction()
            val destination = System.getenv("TUBEPREVIEW_SCREENSHOTS")
            val file = if (destination != null) {
                File(destination, "TubePreview-feedback-${android.os.Build.VERSION.SDK_INT}-${if (theme) "dark" else "light"}.png")
            } else File.createTempFile("feed-feedback", ".png")
            try {
                compose.onRoot().captureRoboImage(file.absolutePath,
                    roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                val bounds = compose.onNodeWithTag("home_feedback").fetchSemanticsNode().boundsInRoot
                val pixel = bitmap.getPixel(bounds.center.x.toInt(), bounds.bottom.toInt() - 6)
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                org.junit.Assert.assertTrue("The scene must remain visible through the scrim",
                    red > green && green > blue)
                val foreground = if (theme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                org.junit.Assert.assertTrue("Feedback text must have readable contrast",
                    androidx.core.graphics.ColorUtils.calculateContrast(foreground, pixel) >= 4.5)
                bitmap.recycle()
            } finally {
                if (destination == null) file.delete()
            }
        }
        compose.onNodeWithContentDescription("Dismiss").performClick()
        compose.onNodeWithText("Demo video · A weekend worth remembering").assertDoesNotExist()
    }
}
