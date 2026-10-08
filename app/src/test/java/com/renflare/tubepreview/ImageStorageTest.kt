package com.renflare.tubepreview

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.ImageStorage
import com.renflare.tubepreview.data.ImageTooLargeException
import com.renflare.tubepreview.data.InvalidImageException
import com.renflare.tubepreview.data.ImageDimensionsTooLargeException
import java.nio.ByteBuffer
import java.util.zip.CRC32
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageStorageTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val storage = ImageStorage(context)

    private fun pngBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            bitmap.recycle()
            output.toByteArray()
        }
    }

    private fun source(bytes: ByteArray): File =
        File.createTempFile("source", ".png", context.cacheDir).apply { writeBytes(bytes) }

    private fun uploadedFiles(): Set<String> = context.filesDir.listFiles()
        .orEmpty().filter { it.name.startsWith("uploaded_image_") }.map { it.name }.toSet()

    @Test fun allowsAnImageExactlyAtTheLimit() {
        val input = source(pngBytes().copyOf(ImageStorage.MAX_IMAGE_BYTES.toInt()))
        try {
            val imported = storage.importImage(Uri.fromFile(input))
            try {
                assertEquals(ImageStorage.MAX_IMAGE_BYTES, imported.length())
            } finally {
                imported.delete()
            }
        } finally {
            input.delete()
        }
    }

    @Test fun rejectsOneByteOverTheLimitAndCleansUp() {
        val before = uploadedFiles()
        val input = source(pngBytes().copyOf(ImageStorage.MAX_IMAGE_BYTES.toInt() + 1))
        try {
            assertThrows(ImageTooLargeException::class.java) {
                storage.importImage(Uri.fromFile(input))
            }
            assertEquals(before, uploadedFiles())
        } finally {
            input.delete()
        }
    }

    @Test fun rejectsInvalidAndEmptyImagesWithoutLeavingFiles() {
        val before = uploadedFiles()
        for (bytes in listOf(byteArrayOf(1, 2, 3), byteArrayOf())) {
            val input = source(bytes)
            try {
                assertThrows(InvalidImageException::class.java) {
                    storage.importImage(Uri.fromFile(input))
                }
            } finally {
                input.delete()
            }
        }
        assertEquals(before, uploadedFiles())
    }

    @Test fun unreadableSourceDoesNotLeaveAnImportedFile() {
        val before = uploadedFiles()
        val input = source(pngBytes())
        input.delete()
        assertThrows(IOException::class.java) { storage.importImage(Uri.fromFile(input)) }
        assertEquals(before, uploadedFiles())
    }

    @Test fun failedReplacementPreservesPreviousImageAndSuccessUsesNewPath() {
        val input = source(pngBytes())
        val previous = storage.importImage(Uri.fromFile(input))
        try {
            val previousBytes = previous.readBytes()
            val oversized = source(ByteArray(ImageStorage.MAX_IMAGE_BYTES.toInt() + 1))
            try {
                assertThrows(ImageTooLargeException::class.java) {
                    storage.importImage(Uri.fromFile(oversized))
                }
                assertArrayEquals(previousBytes, previous.readBytes())
            } finally {
                oversized.delete()
            }
            val replacement = storage.importImage(Uri.fromFile(input))
            try {
                assertNotEquals(previous.absolutePath, replacement.absolutePath)
                assertTrue(previous.exists())
            } finally {
                replacement.delete()
            }
        } finally {
            previous.delete()
            input.delete()
        }
    }

    @Test fun validatesDimensionsWithoutIntegerOverflow() {
        ImageStorage.validateImageBounds(8000, 5000) // Exactly 40 megapixels.
        assertThrows(ImageDimensionsTooLargeException::class.java) {
            ImageStorage.validateImageBounds(8001, 5000)
        }
        assertThrows(ImageDimensionsTooLargeException::class.java) {
            ImageStorage.validateImageBounds(ImageStorage.MAX_IMAGE_DIMENSION + 1, 1)
        }
        assertThrows(ImageDimensionsTooLargeException::class.java) {
            ImageStorage.validateImageBounds(Int.MAX_VALUE, Int.MAX_VALUE)
        }
        assertThrows(InvalidImageException::class.java) {
            ImageStorage.validateImageBounds(0, 1)
        }
    }

    @Test fun rejectsHugeCompressedImageHeadersAndCleansUp() {
        val before = uploadedFiles()
        // An attacker-controlled PNG header can claim huge dimensions in a tiny file.
        val bytes = pngBytes()
        ByteBuffer.wrap(bytes, 16, 8).putInt(65_535).putInt(65_535)
        val crc = CRC32().apply { update(bytes, 12, 17) }.value.toInt()
        ByteBuffer.wrap(bytes, 29, 4).putInt(crc)
        val input = source(bytes)
        try {
            assertThrows(ImageDimensionsTooLargeException::class.java) {
                storage.importImage(Uri.fromFile(input))
            }
            assertEquals(before, uploadedFiles())
        } finally {
            input.delete()
        }
    }

    @Test fun onlyDeletesOwnedImportedImages() {
        val outside = source(pngBytes())
        val unrelated = File(context.filesDir, "unrelated.txt").apply { writeText("keep") }
        val owned = File.createTempFile("uploaded_image_", ".image", context.filesDir)
        try {
            storage.deleteImportedImage(outside.absolutePath)
            storage.deleteImportedImage(unrelated.absolutePath)
            storage.deleteImportedImage(owned.absolutePath)
            assertTrue(outside.exists())
            assertTrue(unrelated.exists())
            assertFalse(owned.exists())
        } finally {
            outside.delete()
            unrelated.delete()
            owned.delete()
        }
    }

    @Test fun countsStreamBytesWithoutDependingOnSizeMetadata() {
        val input = ByteArrayInputStream(ByteArray(ImageStorage.MAX_IMAGE_BYTES.toInt() + 100_000))
        val output = ByteArrayOutputStream()
        assertThrows(ImageTooLargeException::class.java) {
            ImageStorage.copyWithinLimit(input, output)
        }
        assertTrue(output.size() <= ImageStorage.MAX_IMAGE_BYTES)
        assertTrue(input.available() > 0) // Stops reading as soon as the bound is exceeded.
    }
}
