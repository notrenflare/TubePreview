package com.renflare.tubepreview.data

import kotlin.random.Random

data class FeedLayout(
    val placeholders: List<FeedVideo> = MockFeedProvider.getPlaceholderVideos(),
    val previewOffset: Int = 1,
    val shorts: List<FeedShort> = MockShortFeed.initial()
)

object FeedComposer {
    fun reshuffle(previous: FeedLayout, settings: CreatorSettings, random: Random = Random.Default): FeedLayout {
        val shuffled = previous.placeholders.shuffled(random).let {
            // Always show a different sample at the top, even after an unlucky shuffle.
            if (it.size > 1 && it.first().id == previous.placeholders.first().id) it.drop(1) + it.first() else it
        }
        val slots = minOf(settings.previewInterval.coerceAtLeast(2), settings.feedSize.coerceAtLeast(1))
        val previousOffset = previous.previewOffset.coerceIn(0, slots - 1)
        val offset = (0 until slots).filter { it != previousOffset }.randomOrNull(random) ?: 0
        return FeedLayout(shuffled, offset, MockShortFeed.refreshed(previous.shorts, random))
    }

    fun compose(settings: CreatorSettings, layout: FeedLayout): List<FeedVideo> {
        val placeholders = layout.placeholders.take(settings.feedSize.coerceAtLeast(0))
        if (placeholders.isEmpty()) return emptyList()
        val interval = settings.previewInterval.coerceAtLeast(2)
        val offset = layout.previewOffset.coerceIn(0, minOf(interval, placeholders.size) - 1)
        val preview = settings.activePreview()
        val result = mutableListOf<FeedVideo>()
        var previewCount = 0
        placeholders.forEachIndexed { index, video ->
            if (index % interval == offset) {
                result += FeedVideo(
                    id = "user_preview_${previewCount++}",
                    title = preview.title,
                    channelName = settings.channelName,
                    channelAvatarUrl = settings.profilePictureUri,
                    previewThumbnailUrl = preview.thumbnailUri,
                    views = settings.previewViews,
                    duration = settings.previewDuration,
                    uploadTime = settings.previewUploadTime,
                    isUserPreview = true,
                    avatarCrop = settings.avatarCrop
                )
            }
            result += video.copy(
                title = settings.placeholderTitleDefault.ifBlank { video.title },
                views = settings.placeholderViewsDefault.ifBlank { video.views },
                duration = settings.placeholderDurationDefault.ifBlank { video.duration }
            )
        }
        return result
    }
}
