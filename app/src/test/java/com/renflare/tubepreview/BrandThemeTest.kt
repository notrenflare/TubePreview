package com.renflare.tubepreview

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BrandThemeTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun defaultThemeHasPureBlackCanvas() {
        lateinit var colors: ColorScheme
        compose.setContent { TubePreviewTheme { colors = MaterialTheme.colorScheme } }
        compose.runOnIdle {
            assertEquals(Color.Black, colors.background)
            assertEquals(Color.Black, colors.surface)
        }
    }

    @Test
    fun explicitLightThemeHasWhiteCanvas() {
        lateinit var colors: ColorScheme
        compose.setContent { TubePreviewTheme(darkTheme = false) { colors = MaterialTheme.colorScheme } }
        compose.runOnIdle { assertEquals(Color.White, colors.background) }
    }

    @Test
    fun brandVariantsPreserveTransparentMarginsAndDistinctOutlines() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dark = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo_dark)
        val light = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo_light)
        try {
            assertEquals(0, AndroidColor.alpha(dark.getPixel(0, 0)))
            assertEquals(0, AndroidColor.alpha(light.getPixel(0, 0)))
            val redOutline = dark.getPixel(512, 310)
            val blackOutline = light.getPixel(512, 310)
            assertTrue(AndroidColor.red(redOutline) > AndroidColor.green(redOutline) * 2)
            assertTrue(AndroidColor.red(blackOutline) < 40)
            assertEquals(255, AndroidColor.alpha(redOutline))
            assertEquals(255, AndroidColor.alpha(blackOutline))
        } finally {
            dark.recycle()
            light.recycle()
        }
    }
}
