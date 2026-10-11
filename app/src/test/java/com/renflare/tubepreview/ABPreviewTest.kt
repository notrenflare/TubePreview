package com.renflare.tubepreview

import com.renflare.tubepreview.data.*
import org.junit.Assert.*
import org.junit.Test

class ABPreviewTest {
    private val settings = CreatorSettings(
        channelName = "Creator", profilePictureUri = "avatar.png", previewThumbnailUri = "a.png",
        isDarkMode = true, feedSize = 15, previewTitle = "Title A", previewViews = "75K",
        previewDuration = "12:15", previewUploadTime = "2 hours ago", previewInterval = 5,
        isOnboarded = true, placeholderTitleDefault = "", placeholderViewsDefault = "", placeholderDurationDefault = "",
        abPreview = ABPreviewSettings(true, ComparisonMode.BOTH,
            PreviewVariant("Title B", "b.png"), PreviewVariant("Title C", "c.png"))
    )

    @Test fun refreshCyclesPairsAndWrapsEveryPreviewCardTogether() {
        var current = settings
        repeat(9) { turn ->
            val index = turn % 3
            assertEquals(index, current.activePreviewIndex())
            val feed = FeedComposer.compose(current, FeedLayout())
            val previews = feed.filter { it.isUserPreview }
            assertEquals(3, previews.size)
            previews.forEach {
                assertEquals("Title ${'A' + index}", it.title)
                assertEquals("${'a' + index}.png", it.previewThumbnailUrl)
                assertEquals("avatar.png", it.channelAvatarUrl)
            }
            current = current.copy(abPreview = current.abPreview.copy(activeIndex = current.nextPreviewIndex()))
        }
    }

    @Test fun titleOnlyKeepsThumbnailAndThumbnailOnlyKeepsTitle() {
        ComparisonMode.entries.forEach { mode ->
            val current = settings.copy(abPreview = settings.abPreview.copy(mode = mode, activeIndex = 2))
            val preview = current.activePreview()
            assertEquals(if (mode == ComparisonMode.THUMBNAILS) "Title A" else "Title C", preview.title)
            assertEquals(if (mode == ComparisonMode.TITLES) "a.png" else "c.png", preview.thumbnailUri)
        }
    }

    @Test fun skipsIncompleteMiddlePairAndAlternatesTwoChoices() {
        var current = settings.copy(abPreview = settings.abPreview.copy(optionB = PreviewVariant("Title B", "")))
        assertEquals(listOf(0, 2), current.readyPreviewIndices())
        repeat(6) { turn ->
            assertEquals(if (turn % 2 == 0) 0 else 2, current.activePreviewIndex())
            current = current.copy(abPreview = current.abPreview.copy(activeIndex = current.nextPreviewIndex()))
        }
    }

    @Test fun whitespaceAndMissingImagesAreCheckedOnlyForTheirMode() {
        val current = settings.copy(abPreview = settings.abPreview.copy(
            optionB = PreviewVariant("  ", "b.png"), optionC = PreviewVariant("Title C", "")))
        assertEquals(listOf(0), current.readyPreviewIndices())
        assertEquals(listOf(0, 2), current.copy(abPreview = current.abPreview.copy(mode = ComparisonMode.TITLES)).readyPreviewIndices())
        assertEquals(listOf(0, 1), current.copy(abPreview = current.abPreview.copy(mode = ComparisonMode.THUMBNAILS)).readyPreviewIndices())
    }

    @Test fun disabledOrEmptyComparisonFallsBackWithoutLosingSavedOptions() {
        val disabled = settings.copy(abPreview = settings.abPreview.copy(enabled = false, activeIndex = 2))
        assertEquals(PreviewVariant("Title A", "a.png"), disabled.activePreview())
        assertEquals(2, disabled.nextPreviewIndex())
        val empty = settings.copy(previewThumbnailUri = "", abPreview = ABPreviewSettings(enabled = true))
        assertEquals(PreviewVariant("Title A", ""), empty.activePreview())
        assertEquals(0, empty.nextPreviewIndex())
    }

    @Test fun removedActiveOptionAndOutOfRangeSelectionResolveSafely() {
        for (index in listOf(1, -1, 50)) {
            val current = settings.copy(abPreview = settings.abPreview.copy(activeIndex = index, optionB = PreviewVariant()))
            assertEquals(0, current.activePreviewIndex())
            assertEquals(2, current.nextPreviewIndex())
        }
    }
}
