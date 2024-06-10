package android.assignment.acharyaprashant.network

import android.assignment.acharyaprashant.AssignmentApp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.min

object ImageLoader {
    private val coroutinescope = CoroutineScope(
        Executors.newFixedThreadPool(6).asCoroutineDispatcher()
    ) // Adjust the number of threads as needed
    private const val MEMORY_CACHE_SIZE = 20
    private val memoryCache = LruCache<String, Bitmap>(MEMORY_CACHE_SIZE)
    private val diskCacheDir = File(AssignmentApp.context.cacheDir, "images")

    init {
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
    }

    fun loadImage(url: String, onSuccess: (Bitmap) -> Unit, onError: () -> Unit): Job {
        return coroutinescope.launch {
            try {
                val bitmap = getBitmapFromCache(url) ?: downloadAndCacheImage(url)
                bitmap?.let {
                    withContext(Dispatchers.Main) {
                        onSuccess(bitmap)
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        onError()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError()
                }
            }
        }
    }

    private fun getBitmapFromCache(url: String): Bitmap? {
        memoryCache.get(url)?.let {
            return it
        }

        val file = File(diskCacheDir, url.hashCode().toString())
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            memoryCache.put(url, bitmap)
            return bitmap
        }

        return null
    }


    private fun createScaledBitmap(
        originalBitmap: Bitmap,
        desiredWidth: Int,
        desiredHeight: Int
    ): Bitmap {
        // Calculate scaling factors
        val scaleFactorWidth = desiredWidth.toFloat() / originalBitmap.width
        val scaleFactorHeight = desiredHeight.toFloat() / originalBitmap.height

        // Use the minimum scaling factor to ensure that the entire image fits within the desired dimensions
        val scaleFactor = min(scaleFactorWidth, scaleFactorHeight)

        // Calculate the final scaled dimensions
        val scaledWidth = (originalBitmap.width * scaleFactor).toInt()
        val scaledHeight = (originalBitmap.height * scaleFactor).toInt()

        // Create the scaled bitmap
        val scaledBitmap =
            Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

        // Recycle the original bitmap to release memory
        originalBitmap.recycle()

        return scaledBitmap
    }

    private fun downloadAndCacheImage(url: String): Bitmap? {
        val inputStream = URL(url).openStream()
        val bitmap = createScaledBitmap(BitmapFactory.decodeStream(inputStream), 300, 300)
        saveBitmapToCache(url, bitmap)
        return bitmap
    }

    private fun saveBitmapToCache(url: String, bitmap: Bitmap) {
        memoryCache.put(url, bitmap)

        val file = File(diskCacheDir, url.hashCode().toString())
        if (!file.exists()) {
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        }
    }
}
