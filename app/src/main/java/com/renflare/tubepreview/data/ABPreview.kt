package com.renflare.tubepreview.data

enum class ComparisonMode(val label: String) {
    TITLES("Titles"), THUMBNAILS("Thumbnails"), BOTH("Both")
}

data class PreviewVariant(val title: String = "", val thumbnailUri: String = "")

data class ABPreviewSettings(
    val enabled: Boolean = false,
    val mode: ComparisonMode = ComparisonMode.BOTH,
    val optionB: PreviewVariant = PreviewVariant(),
    val optionC: PreviewVariant = PreviewVariant(),
    val activeIndex: Int = 0
) {
    fun variants(base: PreviewVariant): List<PreviewVariant> = listOf(base, optionB, optionC)

    fun readyIndices(base: PreviewVariant): List<Int> = variants(base).mapIndexedNotNull { index, variant ->
        val ready = when (mode) {
            ComparisonMode.TITLES -> variant.title.isNotBlank()
            ComparisonMode.THUMBNAILS -> variant.thumbnailUri.isNotBlank()
            ComparisonMode.BOTH -> variant.title.isNotBlank() && variant.thumbnailUri.isNotBlank()
        }
        index.takeIf { ready }
    }

    fun nextIndex(base: PreviewVariant): Int {
        if (!enabled) return activeIndex
        val ready = readyIndices(base)
        if (ready.isEmpty()) return 0
        val current = activeIndex.takeIf { it in ready } ?: ready.first()
        return ready[(ready.indexOf(current) + 1) % ready.size]
    }
}

// Option A is the ordinary preview, so disabling comparison restores that preview.
fun CreatorSettings.previewVariants(): List<PreviewVariant> = listOf(
    PreviewVariant(previewTitle, previewThumbnailUri), abPreview.optionB, abPreview.optionC
)

fun CreatorSettings.readyPreviewIndices(): List<Int> = abPreview.readyIndices(previewVariants().first())

fun CreatorSettings.activePreviewIndex(): Int =
    if (!abPreview.enabled) 0 else readyPreviewIndices().let { ready ->
        abPreview.activeIndex.takeIf { it in ready } ?: ready.firstOrNull() ?: 0
    }

fun CreatorSettings.nextPreviewIndex(): Int = abPreview.nextIndex(previewVariants().first())

fun CreatorSettings.activePreview(): PreviewVariant {
    val base = previewVariants().first()
    if (!abPreview.enabled || readyPreviewIndices().isEmpty()) return base
    val option = previewVariants()[activePreviewIndex()]
    return when (abPreview.mode) {
        ComparisonMode.TITLES -> base.copy(title = option.title)
        ComparisonMode.THUMBNAILS -> base.copy(thumbnailUri = option.thumbnailUri)
        ComparisonMode.BOTH -> option
    }
}

fun CreatorSettings.imageUris(): Set<String> =
    (previewVariants().map { it.thumbnailUri } + profilePictureUri).filter { it.isNotBlank() }.toSet()
