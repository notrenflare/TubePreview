package com.renflare.tubepreview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.ui.components.ABPreviewToggle
import com.renflare.tubepreview.ui.components.ImageWarningDialog
import com.renflare.tubepreview.ui.components.PlumpIcons
import com.renflare.tubepreview.ui.screens.SectionHeader
import androidx.core.graphics.ColorUtils
import com.renflare.tubepreview.ui.theme.BrandRed
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
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
class SettingsPolishTest {
    @get:Rule val compose = createComposeRule()
    private fun capture(node: SemanticsNodeInteraction): Bitmap {
        val file = File.createTempFile("settings-polish", ".png")
        try {
            node.captureRoboImage(filePath = file.absolutePath,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
            return BitmapFactory.decodeFile(file.absolutePath)
        } finally { file.delete() }
    }

    @Test fun sectionBadgeStaysVisibleAgainstBothCanvases() {
        val dark = mutableStateOf(false)
        var backdrop = Color.White
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                backdrop = MaterialTheme.colorScheme.background
                Column(Modifier.background(backdrop).padding(20.dp)) {
                    SectionHeader(PlumpIcons.UserSolid, "Channel")
                }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            val bitmap = capture(compose.onNodeWithTag("section_icon_Channel"))
            try {
                val badge = bitmap.getPixel(bitmap.width / 2, 5)
                assertTrue("Section badge must remain visible, dark=$theme",
                    ColorUtils.calculateContrast(badge, backdrop.toArgb()) > if (theme) 1.4 else 1.25)
            } finally { bitmap.recycle() }
        }
    }

    @Test fun warningIsCompactReadableAndDismissibleInBothThemes() {
        val dark = mutableStateOf(false)
        val visible = mutableStateOf(true)
        val message = "Choose an image that is 2 MB or smaller. Try compressing it or exporting a smaller copy, then choose it again."
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                if (visible.value) ImageWarningDialog(message) { visible.value = false }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            compose.onNodeWithText(message).assertIsDisplayed()
            compose.onNodeWithTag("image_warning_dismiss").assertIsDisplayed()
            val bounds = compose.onNodeWithTag("image_warning_dialog").getUnclippedBoundsInRoot()
            val height = bounds.bottom - bounds.top
            assertTrue("Warning should wrap its content, height=$height", height < 260.dp)
        }
        compose.onNodeWithTag("image_warning_dismiss").performClick()
        compose.onNodeWithTag("image_warning_dialog").assertDoesNotExist()
    }

    @Test fun subtitleFirstLetterIsFullyVisibleInBothThemes() {
        val dark = mutableStateOf(false)
        var backdrop = Color.White
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                CompositionLocalProvider(LocalDensity provides Density(3f, 1.08f)) {
                    backdrop = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        .compositeOver(MaterialTheme.colorScheme.background)
                    Column(Modifier.fillMaxWidth().background(backdrop).padding(40.dp)) {
                        ABPreviewToggle(false) {}
                        Spacer(Modifier.height(24.dp))
                        Text("Compare titles and thumbnails", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(228.dp).testTag("reference_subtitle"))
                    }
                }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            val actual = capture(compose.onAllNodesWithText("Compare titles and thumbnails", useUnmergedTree = true).onFirst())
            val reference = capture(compose.onNodeWithTag("reference_subtitle"))
            fun ink(bitmap: Bitmap): Int {
                val bg = backdrop.toArgb()
                return (0 until bitmap.height).sumOf { y ->
                    (0 until minOf(27, bitmap.width)).count { x -> bitmap.getPixel(x, y) != bg }
                }
            }
            try {
                assertTrue("First C is clipped, dark=$theme: ${ink(actual)} vs ${ink(reference)}",
                    ink(actual) >= ink(reference) * 0.96)
            } finally { actual.recycle(); reference.recycle() }
        }
    }

    @Test fun offSwitchHasNeutralTrackInBothThemes() {
        val dark = mutableStateOf(false)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(40.dp)) {
                    ABPreviewToggle(false) {}
                }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            val actual = capture(compose.onNodeWithTag("ab_toggle"))
            try {
                val pixel = actual.getPixel(actual.width - 35, actual.height / 2)
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                assertTrue("Off track must be neutral, dark=$theme: $red,$green,$blue",
                    maxOf(red, green, blue) - minOf(red, green, blue) <= 3)
            } finally { actual.recycle() }
        }
    }

    @Test fun enabledSwitchUsesBrandRedInsteadOfDisabledTintInBothThemes() {
        val dark = mutableStateOf(false)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(40.dp)) {
                    ABPreviewToggle(true) {}
                }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            val actual = capture(compose.onNodeWithTag("ab_toggle"))
            try {
                val redPixels = (0 until actual.height).sumOf { y ->
                    (actual.width - 156 until actual.width).count { x -> actual.getPixel(x, y) == BrandRed.toArgb() }
                }
                assertTrue("Checked switch should have a solid brand-red track, dark=$theme, pixels=$redPixels", redPixels > 1500)
            } finally { actual.recycle() }
        }
    }
}
