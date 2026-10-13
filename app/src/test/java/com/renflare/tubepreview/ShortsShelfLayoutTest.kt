package com.renflare.tubepreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.data.MockFeedProvider
import com.renflare.tubepreview.data.MockShortFeed
import com.renflare.tubepreview.ui.screens.HomeScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
class ShortsShelfLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun lazyShelfPreservesCardSizesGapsAndFollowingVideoInBothThemes() {
        val dark = mutableStateOf(true)
        val shorts = MockShortFeed.initial()
        val videos = MockFeedProvider.getPlaceholderVideos().take(7)
        val settings = CreatorSettings("renflare", "", "", true, 15, "", "", "", "", 5, false, "", "", "")
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                HomeScreen(settings.copy(isDarkMode = dark.value), videos, false, {}, {}, {},
                    shortsFeed = shorts)
            }
        }
        for (theme in listOf(true, false)) {
            compose.runOnIdle { dark.value = theme }
            compose.onNodeWithTag("home_video_feed_list")
                .performScrollToNode(hasTestTag("home_shorts_shelf"))
            val header = compose.onNodeWithTag("home_shorts_shelf").getUnclippedBoundsInRoot()
            val cards = shorts.map { compose.onNodeWithTag("home_short_${it.id}").getUnclippedBoundsInRoot() }
            assertEquals(44f, (header.bottom - header.top).value, 1f)
            assertEquals(header.bottom.value, cards[0].top.value, 1f)
            assertEquals(12f, cards[0].left.value, 1f)
            assertEquals(164f, (cards[0].right - cards[0].left).value, 1f)
            assertEquals(246f, (cards[0].bottom - cards[0].top).value, 1f)
            assertEquals(8f, (cards[1].left - cards[0].right).value, 1f)
            assertEquals(8f, (cards[2].top - cards[0].bottom).value, 1f)
            assertEquals(cards[0].left.value, cards[2].left.value, 1f)
            compose.onNodeWithTag("home_video_feed_list")
                .performScrollToNode(hasTestTag("video_card_${videos[6].id}"))
            val lastRow = compose.onNodeWithTag("home_short_${shorts[2].id}").getUnclippedBoundsInRoot()
            val following = compose.onNodeWithTag("video_card_${videos[6].id}").getUnclippedBoundsInRoot()
            assertEquals(20f, (following.top - lastRow.bottom).value, 1f)
        }
    }
}
