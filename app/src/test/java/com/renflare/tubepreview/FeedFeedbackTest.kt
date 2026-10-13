package com.renflare.tubepreview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.data.FeedVideo
import com.renflare.tubepreview.ui.screens.HomeScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 36])
class FeedFeedbackTest {
    @get:Rule val compose = createComposeRule()
    private val settings = CreatorSettings(
        channelName = "My channel", profilePictureUri = "", previewThumbnailUri = "",
        isDarkMode = true, feedSize = 2, previewTitle = "My preview", previewViews = "75K views",
        previewDuration = "12:15", previewUploadTime = "2 hours ago", previewInterval = 5,
        isOnboarded = true, placeholderTitleDefault = "", placeholderViewsDefault = "",
        placeholderDurationDefault = ""
    )
    private val feed = listOf(
        FeedVideo(id = "demo", title = "Sample video", channelName = "Creator",
            views = "100K views", duration = "10:00", uploadTime = "1 day ago"),
        FeedVideo(id = "preview", title = "My preview", channelName = "My channel",
            views = "75K views", duration = "12:15", uploadTime = "2 hours ago", isUserPreview = true)
    )

    private fun showHome(onSettings: () -> Unit = {}) {
        compose.setContent {
            TubePreviewTheme {
                HomeScreen(settings, feed, false, {}, onSettings, {})
            }
        }
    }

    @Test fun tappingOwnCardOpensSettingsWithoutAFeedbackPopup() {
        var openings = 0
        showHome { openings++ }
        compose.onNodeWithTag("user_video_card").performScrollTo().performClick()
        compose.onNodeWithText("Opening Settings", substring = true).assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, openings) }
    }

    @Test fun fifteenRapidDemoTapsLeaveNoMessagesAfterDismissal() {
        showHome()
        compose.mainClock.autoAdvance = false
        repeat(15) { compose.onNodeWithTag("video_card_demo").performClick() }
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithContentDescription("Dismiss").performClick()
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithText("Demo", substring = true).assertDoesNotExist()
    }

    @Test fun repeatedTapsDoNotExtendTheOriginalTimeout() {
        showHome()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("video_card_demo").performClick()
        compose.mainClock.advanceTimeBy(3500)
        repeat(15) { compose.onNodeWithTag("video_card_demo").performClick() }
        compose.mainClock.advanceTimeBy(1500)
        compose.onNodeWithText("Demo", substring = true).assertDoesNotExist()
    }

    @Test fun openingSettingsClearsVisibleDemoFeedback() {
        showHome()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("video_card_demo").performClick()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithTag("home_preview_settings").performClick()
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithText("Demo", substring = true).assertDoesNotExist()
    }

    @Test fun newerFeedbackReplacesTheVisibleMessage() {
        showHome()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("video_card_demo").performClick()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithTag("home_notifications").performClick()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("No notifications yet").assertIsDisplayed()
        compose.onNodeWithText("Demo", substring = true).assertDoesNotExist()
    }
}
