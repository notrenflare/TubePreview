package com.renflare.tubepreview.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ImageTooLargeException : IOException()
class InvalidImageException : IOException()
class ImageDimensionsTooLargeException : IOException()

/** Imports a bounded, readable image without overwriting the currently selected image. */
class ImageStorage(private val context: Context) {
    companion object {
        const val MAX_IMAGE_BYTES = 2L * 1024 * 1024
        const val MAX_IMAGE_PIXELS = 40_000_000L
        const val MAX_IMAGE_DIMENSION = 16_384

        internal fun validateImageBounds(width: Int, height: Int) {
            if (width <= 0 || height <= 0) throw InvalidImageException()
            // Compressed byte size alone does not bound image dimensions or decoder work.
            if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION ||
                width.toLong() * height > MAX_IMAGE_PIXELS
            ) throw ImageDimensionsTooLargeException()
        }

        // Count actual bytes: cloud/document providers do not always report a reliable size.
        internal fun copyWithinLimit(input: InputStream, output: OutputStream) {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                total += count
                if (total > MAX_IMAGE_BYTES) throw ImageTooLargeException()
                output.write(buffer, 0, count)
            }
        }
    }

    fun importImage(uri: Uri): File {
        // A unique path also prevents Coil from showing a cached version of the old image.
        val imported = File.createTempFile("uploaded_image_", ".image", context.filesDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                imported.outputStream().use { output -> copyWithinLimit(input, output) }
            } ?: throw IOException("Cannot open selected image")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imported.absolutePath, bounds)
            validateImageBounds(bounds.outWidth, bounds.outHeight)
            return imported
        } catch (error: Exception) {
            imported.delete()
            throw error
        }
    }

    fun deleteImportedImage(path: String) {
        if (path.isEmpty()) return
        val file = File(path)
        if (file.parentFile == context.filesDir &&
            (file.name.startsWith("uploaded_image_") ||
                file.name == "profile_pic.png" || file.name == "preview_thumbnail.png")
        ) {
            file.delete()
        }
    }
}
