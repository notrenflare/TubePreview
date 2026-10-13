package com.renflare.tubepreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.screens.OnboardingScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 36])
class OnboardingThemeTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun switchingThemePreservesTheDraftAndSavesTheProfile() {
        val settings = mutableStateOf(CreatorSettings(
            channelName = "renflare", profilePictureUri = "", previewThumbnailUri = "",
            isDarkMode = true, feedSize = 15, previewTitle = "", previewViews = "",
            previewDuration = "", previewUploadTime = "", previewInterval = 5,
            isOnboarded = false, placeholderTitleDefault = "", placeholderViewsDefault = "",
            placeholderDurationDefault = ""
        ))
        var savedName: String? = null
        var navigated = false
        compose.setContent {
            TubePreviewTheme(darkTheme = settings.value.isDarkMode) {
                OnboardingScreen(
                    settings = settings.value,
                    onSaveProfile = { name, _ -> savedName = name },
                    onNavigateNext = { navigated = true },
                    onToggleTheme = { settings.value = settings.value.copy(isDarkMode = it) }
                )
            }
        }
        compose.onNodeWithTag("onboarding_skip").performClick()
        compose.onNodeWithTag("onboarding_name_input").performTextReplacement("  My channel  ")
        compose.onNodeWithContentDescription("Switch to light mode").performClick()
        compose.onNodeWithContentDescription("Switch to dark mode").assertHasClickAction()
        compose.onNodeWithTag("onboarding_name_input").assertTextContains("  My channel  ")
        compose.runOnIdle { assertEquals(false, settings.value.isDarkMode) }

        compose.onNodeWithContentDescription("Switch to dark mode").performClick()
        compose.onNodeWithContentDescription("Switch to light mode").assertExists()
        compose.onNodeWithTag("onboarding_save_button").performClick()
        compose.runOnIdle {
            assertEquals(true, settings.value.isDarkMode)
            assertEquals("My channel", savedName)
            assertEquals(true, navigated)
        }
    }
}
