package com.hudlauncher.app.launcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

/** Keeps one user-picked wallpaper, downscaled, in app-private storage (no storage permission needed). */
class WallpaperStore(private val context: Context) {

    private val file get() = File(context.filesDir, "wallpaper.jpg")

    fun import(uri: Uri) {
        val bitmap = decode(uri)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    }

    fun load(): ImageBitmap? =
        if (file.exists()) BitmapFactory.decodeFile(file.path)?.asImageBitmap() else null

    fun delete() {
        file.delete()
    }

    private fun decode(uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = maxOf(info.size.width, info.size.height)
                if (longest > MAX_SIDE) {
                    val k = MAX_SIDE.toFloat() / longest
                    decoder.setTargetSize((info.size.width * k).toInt(), (info.size.height * k).toInt())
                }
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: error("Cannot decode image")
    }

    private companion object {
        const val MAX_SIDE = 2000
    }
}
