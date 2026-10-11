package com.renflare.tubepreview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.AvatarCrop
import com.renflare.tubepreview.ui.components.AvatarCropDialog
import com.renflare.tubepreview.ui.components.AvatarPhoto
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
class AvatarPhotoUiTest {
    @get:Rule val compose = createComposeRule()

    private fun photo(): File {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File.createTempFile("avatar_crop_test", ".png", context.cacheDir)
        val bitmap = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        listOf(android.graphics.Color.RED, android.graphics.Color.GREEN, android.graphics.Color.BLUE).forEachIndexed { index, color ->
            canvas.drawRect(index * 100f, 0f, (index + 1) * 100f, 100f, Paint().apply { this.color = color })
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    @Test fun sameFramingRendersAtAllAvatarSizesAndUpdatesWithoutChangingPhoto() {
        val file = photo()
        val framing = mutableStateOf(AvatarCrop())
        val loaded = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
        try {
            compose.setContent {
                TubePreviewTheme {
                    Row {
                        listOf(24, 36, 72).forEach { size ->
                            AvatarPhoto(file.absolutePath, framing.value, null,
                                Modifier.size(size.dp).testTag("photo_$size"),
                                onLoaded = { loaded.add(size) }, onError = { throw AssertionError("Test photo could not be loaded") })
                        }
                    }
                }
            }
            compose.waitForIdle()
            compose.waitUntil(10000) {
                compose.onNodeWithTag("photo_72").fetchSemanticsNode()
                loaded.size == 3
            }
            fun assertCenters(blue: Boolean) {
                listOf(24, 36, 72).forEach { size ->
                    val capture = File.createTempFile("avatar_render", ".png")
                    try {
                        compose.onNodeWithTag("photo_$size").captureRoboImage(
                            filePath = capture.absolutePath,
                            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
                        )
                        val bitmap = BitmapFactory.decodeFile(capture.absolutePath)
                        val color = Color(bitmap.getPixel(bitmap.width / 2, bitmap.height / 2))
                        bitmap.recycle()
                        assertTrue("Avatar size $size center: $color", if (blue) color.blue > 0.9f && color.green < 0.1f
                                   else color.green > 0.9f && color.blue < 0.1f)
                    } finally { capture.delete() }
                }
            }
            assertCenters(false)
            compose.runOnIdle { framing.value = AvatarCrop(horizontalBias = 1f, zoom = 2f) }
            assertCenters(true)
        } finally { file.delete() }
    }

    @Test fun editorSavesDragAndZoomButCancelDoesNotSaveReset() {
        val file = photo()
        var saved: AvatarCrop? = null
        var dismissals = 0
        val initial = AvatarCrop()
        try {
            compose.setContent {
                TubePreviewTheme {
                    AvatarCropDialog(file.absolutePath, initial,
                        onDismiss = { dismissals++ }, onSave = { saved = it })
                }
            }
            compose.waitUntil(10000) {
                compose.onAllNodesWithTag("avatar_crop_save").fetchSemanticsNodes().any {
                    !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)
                }
            }
            compose.onNodeWithTag("avatar_crop_zoom").performSemanticsAction(SemanticsActions.SetProgress) { it(2f) }
            compose.onNodeWithTag("avatar_crop_preview").performTouchInput {
                swipe(start = center, end = center.copy(x = center.x - width / 4f), durationMillis = 400)
            }
            compose.onNodeWithTag("avatar_crop_save").performScrollTo().assertIsEnabled().performClick()
            assertEquals(2f, saved!!.zoom, 0.01f)
            assertTrue(saved!!.horizontalBias > 0f)
            val firstSave = saved
            compose.onNodeWithTag("avatar_crop_reset").performScrollTo().performClick()
            compose.onNodeWithTag("avatar_crop_cancel").performScrollTo().performClick()
            assertEquals(firstSave, saved)
            assertEquals(1, dismissals)
        } finally { file.delete() }
    }
}
