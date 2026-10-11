package com.renflare.tubepreview

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.ui.components.*
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
class FeedBackdropTest {
    @get:Rule val compose = createComposeRule()

    @Test fun blurStaysInsideTheBarIncludingItsBottomInset() {
        val dark = mutableStateOf(false)
        compose.setContent {
            val backdrop = rememberFeedBackdrop()
            val list = rememberLazyListState()
            val tint = if (dark.value) Color(0xFF0F0F0F).copy(alpha = .72f)
                else Color.White.copy(alpha = .82f)
            Box(Modifier.fillMaxSize().background(Color.Magenta).testTag("scene")) {
                Box(Modifier.fillMaxSize().feedBackdropSource(backdrop, list).clipToBounds().drawBehind {
                    drawRect(Color.Red)
                })
                // A green occluder distinguishes a transparent blur edge from the recorded feed.
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(80.dp)
                    .background(Color.Green))
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(80.dp)
                    .feedBackdrop(backdrop, tint).testTag("bar"))
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            val file = File.createTempFile("feed-backdrop", ".png")
            try {
                compose.onNodeWithTag("scene").captureRoboImage(file.absolutePath,
                    roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val bar = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot
                val x = bitmap.width / 2
                // The scrim must cover the entire bar and the backdrop must never escape it.
                for (y in bar.top.toInt() + 10 until bitmap.height - 3) {
                    val c = bitmap.getPixel(x, y)
                    val g = android.graphics.Color.green(c)
                    assertTrue("Untinted feed leaked at y=$y, dark=$theme, green=$g",
                        if (theme) g in 8..20 else g in 200..220)
                    assertTrue("Backdrop lost opacity at y=$y, dark=$theme, color=$c",
                        android.graphics.Color.red(c) >= if (theme) 80 else 253)
                }
                // Content above the bar must remain sharp and untinted.
                val above = bitmap.getPixel(x, bar.top.toInt() - 80)
                assertTrue(android.graphics.Color.green(above) == 0)
                bitmap.recycle()
            } finally { file.delete() }
        }
    }
}
