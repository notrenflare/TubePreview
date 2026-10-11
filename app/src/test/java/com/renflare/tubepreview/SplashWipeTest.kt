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
    fun playheadAndBarTailMeetAtCenterWithoutOvershooting() {
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
        fun frame(name: String): java.awt.image.BufferedImage {
            val path = File(frames.root, "convergence-$name.png").absolutePath
            compose.onRoot().captureRoboImage(filePath = path,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
            return ImageIO.read(File(path))
        }
        fun redLine(image: java.awt.image.BufferedImage): List<Int> =
            (0 until image.width).filter { x ->
                val pixel = image.getRGB(x, image.height / 2)
                ((pixel ushr 16) and 255) > 204 && ((pixel ushr 8) and 255) < 102
            }
        fun playTip(image: java.awt.image.BufferedImage): Int =
            (0 until image.width).last { x ->
                val pixel = image.getRGB(x, image.height / 2)
                ((pixel ushr 16) and 255) > 235 && ((pixel ushr 8) and 255) > 235
            }
        val first = frame("start")
        val fullLine = redLine(first)
        assertTrue(fullLine.isNotEmpty())
        var previousTip = playTip(first)
        var previousTail = fullLine.last()
        for (position in listOf(0.25f, 0.5f, 0.75f)) {
            compose.runOnIdle { sweep.value = position }
            val image = frame("$position")
            val line = redLine(image)
            val tip = playTip(image)
            assertTrue("The playhead must advance right", tip > previousTip)
            assertTrue("The tail must retract left", line.last() < previousTail)
            assertTrue("The bar must shrink proportionally",
                kotlin.math.abs(line.size - fullLine.size * (1f - position)) < 5)
            assertTrue("The playhead stays to the left of the remaining bar", tip <= line.first())
            previousTip = tip
            previousTail = line.last()
        }
        compose.runOnIdle { sweep.value = 1f }
        val final = frame("end")
        assertTrue(redLine(final).isEmpty())
        assertTrue("The play tip finishes at center, not the far right",
            kotlin.math.abs(playTip(final) - final.width / 2) <= 3)
    }
}
