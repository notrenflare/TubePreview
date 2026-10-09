package com.renflare.tubepreview

import androidx.compose.ui.geometry.Offset
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
@Config(sdk = [36])
class HomeChromeTest {
    @get:Rule val compose = createComposeRule()
    private val settings = CreatorSettings(
        channelName = "renflare", profilePictureUri = "", previewThumbnailUri = "",
        isDarkMode = true, feedSize = 30, previewTitle = "My video", previewViews = "75K views",
        previewDuration = "12:15", previewUploadTime = "2 hours ago", previewInterval = 5,
        isOnboarded = true, placeholderTitleDefault = "", placeholderViewsDefault = "",
        placeholderDurationDefault = ""
    )
    private val feed = (0 until 30).map {
        FeedVideo(
            id = "test_$it", title = "Video $it", channelName = "Creator",
            views = "100K views", duration = "10:00", uploadTime = "1 day ago",
            isUserPreview = it == 1
        )
    }

    private fun showHome() {
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                HomeScreen(settings, feed, false, {}, {}, {})
            }
        }
    }

    @Test fun headerActionsAreVisibleWithoutOpeningAMenu() {
        showHome()
        compose.onNodeWithContentDescription("Toggle theme").assertIsDisplayed()
        compose.onNodeWithContentDescription("Notifications").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search videos").assertIsDisplayed()
        compose.onNodeWithContentDescription("More feed options").assertDoesNotExist()
    }

    @Test fun barsStayPinnedWhilePullIsPartiallyReversed() {
        showHome()
        val top = compose.onNodeWithTag("home_top_bar").fetchSemanticsNode().boundsInRoot
        val bottom = compose.onNodeWithTag("home_bottom_bar").fetchSemanticsNode().boundsInRoot
        val list = compose.onNodeWithTag("home_video_feed_list")
        list.performTouchInput {
            down(center)
            moveBy(Offset(0f, 100f), delayMillis = 100)
        }
        compose.onNodeWithTag("home_refresh_indicator").assertExists()
        list.performTouchInput { moveBy(Offset(0f, -20f), delayMillis = 100) }
        assertEquals(top, compose.onNodeWithTag("home_top_bar").fetchSemanticsNode().boundsInRoot)
        assertEquals(bottom, compose.onNodeWithTag("home_bottom_bar").fetchSemanticsNode().boundsInRoot)
        list.performTouchInput { up() }
    }

    @Test fun bottomBarReturnsAtFeedEndAndHidesWhenLeavingIt() {
        showHome()
        val bottom = compose.onNodeWithTag("home_bottom_bar").fetchSemanticsNode().boundsInRoot
        val list = compose.onNodeWithTag("home_video_feed_list")
        list.performScrollToIndex(feed.size)
        list.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10000f) }
        compose.waitForIdle()
        val atEnd = list.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange]
        assertEquals(atEnd.maxValue(), atEnd.value(), 0.1f)
        assertEquals(bottom, compose.onNodeWithTag("home_bottom_bar").fetchSemanticsNode().boundsInRoot)
        list.performTouchInput {
            swipeDown(startY = height * 0.3f, endY = height * 0.8f, durationMillis = 600)
        }
        compose.onNodeWithTag("home_bottom_bar").assertIsNotDisplayed()
    }

    @Test fun emptyThumbnailHasOneActionAndExploreComesBeforeAll() {
        showHome()
        compose.onNodeWithText("Tap here to customize your card").assertExists()
        compose.onNodeWithText("Your Thumbnail").assertDoesNotExist()
        compose.onNodeWithText("YOUR PREVIEW").assertDoesNotExist()
        val explore = compose.onNodeWithContentDescription("Explore").fetchSemanticsNode().boundsInRoot
        val all = compose.onNodeWithText("All").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue(explore.left < all.left)
    }
}
