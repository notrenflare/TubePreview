package com.renflare.tubepreview

import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.data.FeedComposer
import com.renflare.tubepreview.data.FeedLayout
import com.renflare.tubepreview.data.MockFeedProvider
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class FeedComposerTest {
    private val settings = CreatorSettings(
        channelName = "David", profilePictureUri = "/files/avatar.png",
        previewThumbnailUri = "/files/thumbnail.png", isDarkMode = true, feedSize = 15,
        previewTitle = "My uploaded video", previewViews = "75K views", previewDuration = "12:15",
        previewUploadTime = "2 hours ago", previewInterval = 5, isOnboarded = true,
        placeholderTitleDefault = "", placeholderViewsDefault = "", placeholderDurationDefault = ""
    )

    @Test fun refreshChangesOrderAndPreviewPositionWithoutLosingImages() {
        var layout = FeedLayout()
        repeat(40) { seed ->
            val before = FeedComposer.compose(settings, layout)
            val next = FeedComposer.reshuffle(layout, settings, Random(seed))
            val after = FeedComposer.compose(settings, next)
            assertNotEquals(layout.placeholders.map { it.id }, next.placeholders.map { it.id })
            assertNotEquals(layout.placeholders.first().id, next.placeholders.first().id)
            assertEquals(layout.placeholders.map { it.id }.toSet(), next.placeholders.map { it.id }.toSet())
            assertNotEquals(before.indexOfFirst { it.isUserPreview }, after.indexOfFirst { it.isUserPreview })
            assertEquals(15, after.count { !it.isUserPreview })
            assertEquals(3, after.count { it.isUserPreview })
            assertEquals(after.size, after.map { it.id }.toSet().size)
            after.filter { it.isUserPreview }.forEach {
                assertEquals(settings.previewThumbnailUri, it.previewThumbnailUrl)
                assertEquals(settings.profilePictureUri, it.channelAvatarUrl)
                assertEquals(settings.previewTitle, it.title)
            }
            layout = next
        }
    }

    @Test fun settingsEditsKeepLayoutAndSamplePhotos() {
        val layout = FeedComposer.reshuffle(FeedLayout(), settings, Random(7))
        val before = FeedComposer.compose(settings, layout)
        val after = FeedComposer.compose(settings.copy(
            isDarkMode = false, placeholderTitleDefault = "Custom title",
            placeholderViewsDefault = "100K views", placeholderDurationDefault = "10:00"
        ), layout)
        assertEquals(before.map { it.id }, after.map { it.id })
        after.filterNot { it.isUserPreview }.forEach {
            assertEquals("Custom title", it.title)
            assertEquals("100K views", it.views)
            assertEquals("10:00", it.duration)
            assertTrue(it.previewThumbnailUrl.startsWith("https://"))
            assertTrue(it.channelAvatarUrl.startsWith("https://"))
        }
    }

    @Test fun smallAndEmptyFeedsStaySafeAndStillIncludePreview() {
        assertTrue(FeedComposer.compose(settings.copy(feedSize = 0), FeedLayout()).isEmpty())
        val smallSettings = settings.copy(feedSize = 1, previewInterval = 0)
        val layout = FeedComposer.reshuffle(FeedLayout(), smallSettings, Random(1))
        val feed = FeedComposer.compose(smallSettings, layout)
        assertEquals(2, feed.size)
        assertTrue(feed.first().isUserPreview)
    }

    @Test fun refreshReplacesEveryShortTitleAndThumbnailTogether() {
        var layout = FeedLayout()
        val seenPairs = mutableMapOf<String, String>()
        repeat(40) { seed ->
            val next = FeedComposer.reshuffle(layout, settings, Random(seed))
            assertEquals(4, next.shorts.size)
            assertEquals(4, next.shorts.map { it.id }.toSet().size)
            assertTrue(layout.shorts.map { it.title }.toSet().intersect(next.shorts.map { it.title }.toSet()).isEmpty())
            assertTrue(layout.shorts.map { it.thumbnailUrl }.toSet().intersect(next.shorts.map { it.thumbnailUrl }.toSet()).isEmpty())
            next.shorts.forEach { short ->
                seenPairs[short.title]?.let { assertEquals(it, short.thumbnailUrl) }
                seenPairs[short.title] = short.thumbnailUrl
            }
            layout = next
        }
        assertEquals(12, seenPairs.size)
    }

    @Test fun savedAvatarFramingIsAppliedOnlyToUserPreviews() {
        val crop = com.renflare.tubepreview.data.AvatarCrop(0.6f, -0.3f, 2f)
        val feed = FeedComposer.compose(settings.copy(avatarCrop = crop), FeedLayout())
        assertTrue(feed.filter { it.isUserPreview }.all { it.avatarCrop == crop })
        assertTrue(feed.filterNot { it.isUserPreview }.all { it.avatarCrop.zoom == 1f })
    }

    @Test fun samplesAreUniqueAndHaveNoCalendarYears() {
        val samples = MockFeedProvider.getPlaceholderVideos()
        assertEquals(30, samples.size)
        assertEquals(30, samples.map { it.title }.toSet().size)
        assertEquals(30, samples.map { it.id }.toSet().size)
        assertFalse(samples.any { Regex("\\b20\\d{2}\\b").containsMatchIn(it.title) })
    }
}
