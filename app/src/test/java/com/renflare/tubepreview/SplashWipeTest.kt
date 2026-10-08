package com.renflare.tubepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.io.File
import javax.imageio.ImageIO
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.renflare.tubepreview.ui.components.SplashArtwork
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class SplashWipeTest {
    @get:Rule val compose = createComposeRule()
    @get:Rule val frames = TemporaryFolder()

    @Test
    fun movingPlayIconErasesTheLineBehindItUntilNothingRemains() {
        val sweep = mutableStateOf(0f)
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                Box(Modifier.background(Color(0xFF0F0F0F))) {
                    SplashArtwork(
                        shrink = 1f, morph = 1f, progress = 1f,
                        background = Color(0xFF0F0F0F), sweep = sweep.value
                    )
                }
            }
        }
        fun remainingLinePixels(frame: String): Int {
            val filePath = File(frames.root, "wipe-$frame.png").absolutePath
            compose.onRoot().captureRoboImage(
                filePath = filePath,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
            )
            val pixels = ImageIO.read(File(filePath))
            val y = pixels.height / 2
            return (0 until pixels.width).count { x ->
                val pixel = pixels.getRGB(x, y)
                val red = (pixel ushr 16) and 255
                val green = (pixel ushr 8) and 255
                red > 204 && green < 102
            }
        }
        val fullLine = remainingLinePixels("start")
        assertTrue(fullLine > 0)
        compose.runOnIdle { sweep.value = 0.5f }
        val halfLine = remainingLinePixels("middle")
        assertTrue(halfLine > fullLine * 0.4f && halfLine < fullLine * 0.7f)
        compose.runOnIdle { sweep.value = 1f }
        assertEquals(0, remainingLinePixels("end"))
    }
}
