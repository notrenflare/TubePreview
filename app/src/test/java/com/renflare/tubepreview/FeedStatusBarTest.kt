package com.renflare.tubepreview

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.ui.components.FeedStatusBarProtection
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
class FeedStatusBarTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun headerHidingRevealsFeedWithoutRecompositionAndRestoresTheScrim() {
        val dark = mutableStateOf(false)
        val hidden = mutableFloatStateOf(0f)
        var compositions = 0
        compose.setContent {
            SideEffect { compositions++ }
            val color = if (dark.value) Color(0xFF0F0F0F) else Color.White
            Box(Modifier.fillMaxSize().background(Color.Red).padding(top = 60.dp).testTag("status_scene")) {
                FeedStatusBarProtection(color, { hidden.floatValue }, Modifier.requiredHeight(30.dp))
            }
        }
        compose.runOnIdle {
            ViewCompat.dispatchApplyWindowInsets(compose.activity.window.decorView,
                WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.statusBars(),
                    Insets.of(0, 90, 0, 0)).build())
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            compose.waitForIdle()
            val before = compositions
            for (fraction in listOf(0f, .5f, 1f, 0f)) {
                compose.runOnIdle { hidden.floatValue = fraction }
                val file = File.createTempFile("feed-status-bar", ".png")
                try {
                    compose.onNodeWithTag("status_scene").captureRoboImage(file.absolutePath,
                        roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val bounds = compose.onNodeWithTag("home_status_bar_protection").fetchSemanticsNode().boundsInRoot
                    val scene = compose.onNodeWithTag("status_scene").fetchSemanticsNode().boundsInRoot
                    val actual = bitmap.getPixel(bitmap.width / 2, (bounds.center.y - scene.top).toInt())
                    val base = if (theme) Color(0xFF0F0F0F) else Color.White
                    val expected = base.copy(alpha = 1f - .4f * fraction).compositeOver(Color.Red).toArgb()
                    for (channel in listOf(
                        android.graphics.Color.red(actual) to android.graphics.Color.red(expected),
                        android.graphics.Color.green(actual) to android.graphics.Color.green(expected),
                        android.graphics.Color.blue(actual) to android.graphics.Color.blue(expected))) {
                        assertEquals("theme=$theme fraction=$fraction bitmap=${bitmap.width}x${bitmap.height} bounds=$bounds actual=$actual", channel.second.toFloat(), channel.first.toFloat(), 1f)
                    }
                    bitmap.recycle()
                } finally { file.delete() }
                compose.runOnIdle { assertEquals("Scroll opacity must be draw-only", before, compositions) }
            }
        }
    }
}
