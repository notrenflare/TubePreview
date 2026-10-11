package com.renflare.tubepreview

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.screens.HomeScreen
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
class ExploreContrastTest {
    @get:Rule val compose = createComposeRule()

    private fun checkSelectedExplore(dark: Boolean) {
        val settings = CreatorSettings(
            channelName = "renflare", profilePictureUri = "", previewThumbnailUri = "",
            isDarkMode = dark, feedSize = 15, previewTitle = "My video", previewViews = "75K",
            previewDuration = "12:15", previewUploadTime = "2 hours ago", previewInterval = 5,
            isOnboarded = true, placeholderTitleDefault = "", placeholderViewsDefault = "", placeholderDurationDefault = ""
        )
        compose.setContent {
            TubePreviewTheme(darkTheme = dark) { HomeScreen(settings, emptyList(), false, {}, {}, {}) }
        }
        compose.onNodeWithTag("home_explore_tab").performClick().assertIsSelected()
        compose.mainClock.advanceTimeBy(1000)
        val image = File.createTempFile("explore_contrast", ".png")
        try {
            compose.onNodeWithContentDescription("Explore").captureRoboImage(
                filePath = image.absolutePath,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
            )
            val bitmap = BitmapFactory.decodeFile(image.absolutePath)
            var darkPixels = 0
            var lightPixels = 0
            // The merged tab also includes touch-target padding. Sample only its centered glyph area.
            val left = bitmap.width / 4
            val top = bitmap.height / 4
            val right = bitmap.width * 3 / 4
            val bottom = bitmap.height * 3 / 4
            for (y in top until bottom) for (x in left until right) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)
                if (brightness < 150) darkPixels++
                if (brightness > 600) lightPixels++
            }
            val area = (right - left) * (bottom - top)
            assertTrue("Selected Explore must keep a contrasting glyph", darkPixels > area * 0.05 && lightPixels > area * 0.05)
            val corner = bitmap.getPixel(left, top)
            assertTrue("Selected background matches theme inversion", if (dark) Color.red(corner) > 200 else Color.red(corner) < 50)
            bitmap.recycle()
        } finally { image.delete() }
    }

    @Test fun selectedExploreIconRemainsVisibleInDarkTheme() = checkSelectedExplore(true)
    @Test fun selectedExploreIconRemainsVisibleInLightTheme() = checkSelectedExplore(false)
}
