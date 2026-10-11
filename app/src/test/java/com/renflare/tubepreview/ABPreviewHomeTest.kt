package com.renflare.tubepreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.renflare.tubepreview.data.*
import com.renflare.tubepreview.ui.screens.HomeScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp")
class ABPreviewHomeTest {
    @get:Rule val compose = createComposeRule()
    private val settings = CreatorSettings(
        channelName = "Creator", profilePictureUri = "", previewThumbnailUri = "",
        isDarkMode = true, feedSize = 1, previewTitle = "Title A", previewViews = "75K",
        previewDuration = "12:15", previewUploadTime = "2 hours ago", previewInterval = 5,
        isOnboarded = true, placeholderTitleDefault = "", placeholderViewsDefault = "", placeholderDurationDefault = "",
        abPreview = ABPreviewSettings(true, ComparisonMode.TITLES, PreviewVariant(), PreviewVariant("Title C"))
    )

    @Test fun activeOptionIsAccessibleIncompleteOptionDisabledAndControlsHideWhenOff() {
        val state = mutableStateOf(settings)
        compose.setContent {
            TubePreviewTheme(darkTheme = state.value.isDarkMode) {
                HomeScreen(
                    state.value, FeedComposer.compose(state.value, FeedLayout()), false, {}, {}, {},
                    onSelectVariant = { state.value = state.value.copy(abPreview = state.value.abPreview.copy(activeIndex = it)) }
                )
            }
        }
        compose.onNodeWithTag("home_ab_0").assertIsSelected()
        compose.onNodeWithTag("home_ab_1").assertIsNotEnabled()
        compose.onNodeWithTag("home_ab_2").performClick().assertIsSelected()
        compose.onNodeWithTag("home_ab_status").assertTextEquals("A/B · C")
        compose.onNodeWithText("Title C").assertExists()
        compose.runOnIdle { state.value = state.value.copy(isDarkMode = false) }
        compose.onNodeWithTag("home_ab_2").assertIsSelected().performClick()
        compose.runOnIdle { state.value = state.value.copy(abPreview = state.value.abPreview.copy(enabled = false)) }
        compose.onNodeWithTag("home_ab_status").assertDoesNotExist()
        compose.onNodeWithText("Title A").assertExists()
    }
}
