package com.renflare.tubepreview

import com.renflare.tubepreview.data.AvatarCrop
import org.junit.Assert.*
import org.junit.Test

class AvatarCropTest {
    @Test fun wideAndTallPhotosCanBeRepositionedWithoutZoom() {
        val wide = AvatarCrop().transform(100f, 100f, 2f, panX = 20f)
        assertEquals(-0.4f, wide.horizontalBias, 0.0001f)
        assertEquals(0f, wide.verticalBias, 0f)
        val tall = AvatarCrop().transform(100f, 100f, 0.5f, panY = -20f)
        assertEquals(0.4f, tall.verticalBias, 0.0001f)
        assertEquals(0f, tall.horizontalBias, 0f)
    }

    @Test fun panCannotExposeBlankSpaceAndZoomIsBounded() {
        val crop = AvatarCrop().transform(100f, 100f, 1f, scale = 2f)
            .transform(100f, 100f, 1f, panX = 10000f, panY = -10000f)
        assertEquals(-1f, crop.horizontalBias, 0f)
        assertEquals(1f, crop.verticalBias, 0f)
        assertEquals(4f, crop.transform(100f, 100f, 1f, scale = 100f).zoom, 0f)
        assertEquals(AvatarCrop(), crop.transform(100f, 100f, 1f, scale = 0.01f))
    }

    @Test fun pinchKeepsTheImagePointUnderTheFingers() {
        val original = AvatarCrop(0.4f, 0f, 1f)
        val focal = 30f
        val beforeLeft = -100f * (1f + original.horizontalBias) / 2f
        val imagePoint = (focal - beforeLeft) / 200f
        val zoomed = original.transform(100f, 100f, 2f, scale = 2f, focalX = focal)
        val afterLeft = -300f * (1f + zoomed.horizontalBias) / 2f
        assertEquals(focal, afterLeft + imagePoint * 400f, 0.001f)
    }

    @Test fun malformedPreferencesAndUnmeasuredViewportsStaySafe() {
        assertEquals(AvatarCrop(), AvatarCrop(Float.NaN, Float.POSITIVE_INFINITY, Float.NaN).sanitized())
        assertEquals(AvatarCrop(1f, -1f, 4f), AvatarCrop(20f, -20f, 99f).sanitized())
        assertEquals(AvatarCrop(), AvatarCrop().transform(0f, 0f, 0f))
    }
}
