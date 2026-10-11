package com.renflare.tubepreview.data

import kotlin.math.max

/** Normalized framing of the original photo, shared by every channel avatar. */
data class AvatarCrop(
    val horizontalBias: Float = 0f,
    val verticalBias: Float = 0f,
    val zoom: Float = 1f
) {
    fun sanitized() = AvatarCrop(
        horizontalBias.takeIf { it.isFinite() }?.coerceIn(-1f, 1f) ?: 0f,
        verticalBias.takeIf { it.isFinite() }?.coerceIn(-1f, 1f) ?: 0f,
        zoom.takeIf { it.isFinite() }?.coerceIn(1f, 4f) ?: 1f
    )

    /** Pan in viewport pixels and zoom around the gesture's focal point. */
    fun transform(
        width: Float, height: Float, imageAspectRatio: Float,
        panX: Float = 0f, panY: Float = 0f, scale: Float = 1f,
        focalX: Float = width / 2f, focalY: Float = height / 2f
    ): AvatarCrop {
        val current = sanitized()
        if (width <= 0f || height <= 0f || !imageAspectRatio.isFinite() || imageAspectRatio <= 0f) return current
        val baseWidth = max(width, height * imageAspectRatio)
        val baseHeight = max(height, width / imageAspectRatio)
        val newZoom = (current.zoom * scale).takeIf { it.isFinite() }?.coerceIn(1f, 4f) ?: current.zoom
        val factor = newZoom / current.zoom
        fun bias(base: Float, viewport: Float, oldBias: Float, pan: Float, focal: Float): Float {
            val oldOverflow = max(0f, base * current.zoom - viewport)
            val newOverflow = max(0f, base * newZoom - viewport)
            if (newOverflow <= 0f) return 0f
            val oldStart = -oldOverflow * (1f + oldBias) / 2f
            val newStart = focal + (oldStart - focal) * factor + pan
            return (-2f * newStart / newOverflow - 1f).coerceIn(-1f, 1f)
        }
        return AvatarCrop(
            bias(baseWidth, width, current.horizontalBias, panX, focalX),
            bias(baseHeight, height, current.verticalBias, panY, focalY),
            newZoom
        ).sanitized()
    }
}
