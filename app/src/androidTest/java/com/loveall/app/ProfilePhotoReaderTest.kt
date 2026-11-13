package com.loveall.app

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.data.ProfilePhotoReader
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ProfilePhotoReaderTest {
    @Test
    fun unsupportedSourceFailsWithValidationError() = runBlocking {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        try {
            ProfilePhotoReader(resolver, Dispatchers.IO).read("file:///unselected-photo.jpg")
            fail("Only a selected content URI should be read")
        } catch (error: AuthException) {
            assertEquals(AuthError.VALIDATION, error.error)
        }
    }

    @Test
    fun selectedContentImageIsResizedOrientedAndStrippedOfGps() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val source = File.createTempFile("photo-reader-", ".jpg", context.cacheDir)
        val bitmap = Bitmap.createBitmap(2000, 1000, Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        ExifInterface(source).apply {
            setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90.toString(),
            )
            setAttribute(ExifInterface.TAG_GPS_LATITUDE, "23/1,30/1,0/1")
            setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S")
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "46/1,36/1,0/1")
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W")
            saveAttributes()
        }
        val uri =
            requireNotNull(
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, source.name)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    },
                )
            )
        try {
            requireNotNull(resolver.openOutputStream(uri)).use { it.write(source.readBytes()) }
            val jpeg = ProfilePhotoReader(resolver, Dispatchers.IO).read(uri.toString())
            val result = requireNotNull(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size))
            assertEquals(800, result.width)
            assertEquals(1600, result.height)
            result.recycle()
            val exif = ByteArrayInputStream(jpeg).use { ExifInterface(it) }
            assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
            assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE))
            assertTrue(jpeg.size <= ProfilePhotoReader.MAX_UPLOAD_BYTES)
        } finally {
            resolver.delete(uri, null, null)
            source.delete()
        }
    }
}
