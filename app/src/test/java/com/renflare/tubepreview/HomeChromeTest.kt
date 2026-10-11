package com.renflare.tubepreview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.input.key.Key
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

@OptIn(ExperimentalTestApi::class)
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

    private fun showHome(refreshing: Boolean = false, onRefresh: () -> Unit = {}, onSettings: () -> Unit = {}) {
        compose.setContent {
            TubePreviewTheme(darkTheme = true) {
                HomeScreen(settings, feed, refreshing, onRefresh, onSettings, {})
            }
        }
    }

    @Test fun keyboardCanFocusAndActivateHeaderAndAvatarIndependently() {
        var settingsOpened = 0
        showHome(onSettings = { settingsOpened++ })
        val header = compose.onNodeWithTag("home_preview_settings")
        header.performSemanticsAction(SemanticsActions.RequestFocus)
        header.assertIsFocused()
        header.performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, settingsOpened)
        val avatar = compose.onAllNodesWithContentDescription("Go to channel Creator").onFirst()
        avatar.performSemanticsAction(SemanticsActions.RequestFocus)
        avatar.assertIsFocused()
        avatar.performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, settingsOpened)
    }

    @Test fun shortPullDoesNotRefreshAndFullPullRequestsOneRefresh() {
        var refreshes = 0
        showHome(onRefresh = { refreshes++ })
        val list = compose.onNodeWithTag("home_video_feed_list")
        list.performTouchInput {
            down(center)
            moveBy(Offset(0f, 80f), delayMillis = 150)
            up()
        }
        compose.waitForIdle()
        assertEquals(0, refreshes)
        compose.onNodeWithTag("home_refresh_indicator").assertDoesNotExist()
        list.performTouchInput {
            down(center)
            moveBy(Offset(0f, 500f), delayMillis = 250)
            up()
        }
        compose.waitForIdle()
        assertEquals(1, refreshes)
    }

    @Test fun feedRemainsScrollableDuringRefresh() {
        showHome(refreshing = true)
        compose.onNodeWithContentDescription("Refreshing feed").assertExists()
        val list = compose.onNodeWithTag("home_video_feed_list")
        list.performTouchInput { swipeUp(durationMillis = 400) }
        val range = list.fetchSemanticsNode().config[
            androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange
        ]
        org.junit.Assert.assertTrue(range.value() > 0f)
    }

    @Test fun headerActionsAreVisibleWithoutOpeningAMenu() {
        showHome()
        compose.onNodeWithContentDescription("Your preview settings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Toggle theme").assertDoesNotExist()
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

    @Test fun barsTrackTheDragWithoutAnAnimationLag() {
        showHome()
        val top = compose.onNodeWithTag("home_top_bar").getUnclippedBoundsInRoot()
        val bottom = compose.onNodeWithTag("home_bottom_bar").getUnclippedBoundsInRoot()
        compose.mainClock.autoAdvance = false
        val list = compose.onNodeWithTag("home_video_feed_list")
        list.performTouchInput {
            down(center)
            moveBy(Offset(0f, -80f), delayMillis = 16)
        }
        compose.mainClock.advanceTimeByFrame()
        val movedTop = compose.onNodeWithTag("home_top_bar").getUnclippedBoundsInRoot()
        val movedBottom = compose.onNodeWithTag("home_bottom_bar").getUnclippedBoundsInRoot()
        val travel = (top.top - movedTop.top).value
        org.junit.Assert.assertTrue("Drag must scroll the feed", travel > 5f)
        assertEquals("Chrome should follow the same consumed drag immediately",
            travel, (movedBottom.top - bottom.top).value, 1f)
        list.performTouchInput { up() }
        compose.mainClock.autoAdvance = true
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
        val custom = compose.onNodeWithText("Your custom feed").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue(explore.left < custom.left && custom.left < all.left)
    }
}
