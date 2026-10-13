package com.renflare.tubepreview

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.SettingsRepository
import com.renflare.tubepreview.ui.components.AppMessageDialog
import com.renflare.tubepreview.ui.screens.SettingsScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
class SettingsDialogTest {
    @get:Rule val compose = createComposeRule()

    private fun textStart(label: String): Float =
        compose.onNodeWithText(label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left

    @Test fun longBodyScrollsWithoutHidingActionsAtLargeFontInBothThemes() {
        val dark = mutableStateOf(false)
        val visible = mutableStateOf(true)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                CompositionLocalProvider(LocalDensity provides Density(3f, 1.6f)) {
                    if (visible.value) {
                        AppMessageDialog("Font license", { visible.value = false }, "Done") {
                            repeat(40) { Text("License paragraph $it: permission and attribution information.") }
                            Text("End of license", Modifier.testTag("license_end"))
                        }
                    }
                }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            compose.onNodeWithText("Font license").assertIsDisplayed()
            compose.onNodeWithText("Done").assertIsDisplayed()
            compose.onNodeWithTag("license_end").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Done").assertIsDisplayed()
        }
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("Font license").assertDoesNotExist()
    }

    @Test fun settingsConfirmationsAndLicenseNavigationPreserveTheirActionsInBothThemes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = runBlocking { SettingsRepository(context).settingsFlow.first() }
        val dark = mutableStateOf(false)
        var resets = 0
        var tours = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                SettingsScreen(
                    settings = settings.copy(isDarkMode = dark.value),
                    onNavigateBack = {}, onUpdateChannelName = {}, onUpdateProfilePic = {},
                    onUpdateThumbnail = {}, onUpdateDarkMode = {}, onUpdateFeedSize = {},
                    onUpdatePreviewTitle = {}, onUpdatePreviewViews = {}, onUpdatePreviewDuration = {},
                    onUpdatePreviewInterval = {}, onUpdatePlaceholderDefaults = { _, _, _ -> },
                    onResetAll = { resets++ }, onReplayTour = { tours++ }
                )
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme }
            compose.onNodeWithTag("reset_metadata_btn").performScrollTo().performClick()
            compose.onNodeWithText("Cancel").assertIsDisplayed().performClick()
            compose.onNodeWithTag("reset_confirmation_dialog").assertDoesNotExist()
            compose.runOnIdle { assertEquals(if (theme) 1 else 0, resets) }
            compose.onNodeWithTag("reset_metadata_btn").performClick()
            compose.onNodeWithText("Reset").assertIsDisplayed().performClick()
            compose.runOnIdle { assertEquals(if (theme) 2 else 1, resets) }
            val tour = compose.onNodeWithTag("settings_product_tour").performScrollTo()
            val tourBounds = tour.getUnclippedBoundsInRoot()
            val screenBounds = compose.onRoot().getUnclippedBoundsInRoot()
            assertTrue("Tour feedback wraps its icon and label",
                (tourBounds.right - tourBounds.left) < (screenBounds.right - screenBounds.left) * 0.8f)
            tour.performClick()
            compose.runOnIdle { assertEquals(if (theme) 2 else 1, tours) }
            val licenses = compose.onNodeWithTag("settings_asset_licenses").performScrollTo()
            val licenseBounds = licenses.getUnclippedBoundsInRoot()
            assertTrue("License feedback wraps its icon and label",
                (licenseBounds.right - licenseBounds.left) < (screenBounds.right - screenBounds.left) * 0.8f)
            licenses.performClick()
            compose.onNodeWithTag("asset_licenses_dialog").assertIsDisplayed()
            val descriptionStart = textStart(
                "Free icons from Streamline, adapted for TubePreview. Licensed under CC BY 4.0."
            )
            for (label in listOf("TubePreview · Apache License 2.0", "Streamline",
                "Creative Commons Attribution 4.0", "Nunito · Open Font License")) {
                assertEquals("License text aligns with $label", descriptionStart, textStart(label), 1.5f)
            }
            compose.onNodeWithText("TubePreview · Apache License 2.0").performClick()
            compose.onNodeWithTag("app_license_dialog").assertIsDisplayed()
            compose.onNodeWithText("Done").assertIsDisplayed().performClick()
            compose.onNodeWithTag("app_license_dialog").assertDoesNotExist()
            compose.onNodeWithTag("settings_asset_licenses").performClick()
            compose.onNodeWithText("Nunito · Open Font License").performScrollTo().performClick()
            compose.onNodeWithTag("font_license_dialog").assertIsDisplayed()
            compose.onNodeWithText("Done").assertIsDisplayed().performClick()
            compose.onNodeWithTag("font_license_dialog").assertDoesNotExist()
        }
    }
}
