package com.renflare.tubepreview

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
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
            assertEquals(Color(0xFFDB2638), colors.primary)
            assertEquals(Color.White, colors.onPrimary)
            assertTrue(ColorUtils.calculateContrast(colors.onPrimary.toArgb(), colors.primary.toArgb()) >= 4.5)
        }
    }

    @Test
    fun explicitLightThemeHasWhiteCanvas() {
        lateinit var colors: ColorScheme
        compose.setContent { TubePreviewTheme(darkTheme = false) { colors = MaterialTheme.colorScheme } }
        compose.runOnIdle {
            assertEquals(Color.White, colors.background)
            assertEquals(Color(0xFFDB2638), colors.primary)
            assertEquals(Color.White, colors.onPrimary)
            assertTrue(ColorUtils.calculateContrast(colors.onPrimary.toArgb(), colors.primary.toArgb()) >= 4.5)
        }
    }

    @Test
    fun sharedLogoPreservesTransparentMarginsAndRedOutline() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo)
        try {
            assertEquals(0, AndroidColor.alpha(logo.getPixel(0, 0)))
            assertEquals(0, AndroidColor.alpha(logo.getPixel(1023, 1023)))
            val outline = logo.getPixel(512, 300)
            assertTrue(AndroidColor.red(outline) > AndroidColor.green(outline) * 2)
            assertEquals(255, AndroidColor.alpha(outline))
        } finally {
            logo.recycle()
        }
    }
}
