package com.renflare.tubepreview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.test.core.app.ApplicationProvider
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LauncherIconTest {
    @Test
    fun adaptiveBackgroundCoversForegroundMarginsWithOpaqueRed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (resource in listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round)) {
            for (size in listOf(24, 48, 96)) {
                val icon = context.getDrawable(resource) as AdaptiveIconDrawable
                icon.setBounds(0, 0, size, size)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                try {
                    icon.background.draw(Canvas(bitmap))
                    // The foreground is inset. Every point behind it must remain
                    // red so circular and rounded-square launcher masks show no halo.
                    for (y in 0 until size) {
                        for (x in 0 until size) {
                            val pixel = bitmap.getPixel(x, y)
                            assertEquals("Launcher background is transparent", 255, Color.alpha(pixel))
                            assertTrue(
                                "Launcher background is not red at ($x, $y)",
                                Color.red(pixel) > Color.green(pixel) * 2 &&
                                    Color.red(pixel) > Color.blue(pixel) * 2
                            )
                        }
                    }
                } finally {
                    bitmap.recycle()
                }
            }
        }
    }

    @Test
    fun eyeFitsSafeAreaAtSmallAndLargeLauncherSizes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (resource in listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round)) {
            for (size in listOf(24, 48, 96)) {
                val icon = context.getDrawable(resource) as AdaptiveIconDrawable
                icon.setBounds(0, 0, size, size)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                try {
                    // Inspect the unmasked foreground so a mask cannot hide clipped artwork.
                    icon.foreground.draw(Canvas(bitmap))
                    var artworkPixels = 0
                    val center = size / 2.0
                    val safeRadius = size * 33.0 / 72.0
                    for (y in 0 until size) {
                        for (x in 0 until size) {
                            val pixel = bitmap.getPixel(x, y)
                            val isEye = Color.alpha(pixel) > 128 &&
                                (Color.green(pixel) > 100 || Color.red(pixel) < 60)
                            if (isEye) {
                                artworkPixels++
                                assertTrue(
                                    "Eye extends beyond the safe area at ${size}px",
                                    hypot(x + 0.5 - center, y + 0.5 - center) <= safeRadius + 1.0
                                )
                            }
                        }
                    }
                    assertTrue("Eye is missing at ${size}px", artworkPixels > size * size / 8)
                } finally {
                    bitmap.recycle()
                }
            }
        }
    }
}
