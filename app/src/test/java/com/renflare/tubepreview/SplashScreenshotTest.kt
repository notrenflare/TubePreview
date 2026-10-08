package com.renflare.tubepreview

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import com.renflare.tubepreview.ui.components.StartupSplash
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SplashScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun capturesLoadingSplash() {
    composeTestRule.setContent { TubePreviewTheme { StartupSplash(isReady = false, onComplete = {}) } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/splash-loading.png")
  }
}
