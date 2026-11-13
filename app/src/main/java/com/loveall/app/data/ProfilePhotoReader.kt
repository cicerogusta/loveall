package com.loveall.app.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.repository.PhotoReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Implementação Android de preparação de fotos. Executa leitura e processamento no dispatcher
 * fornecido, corrige a orientação e cria um novo JPEG sem copiar o EXIF original. use fecha streams
 * e finally libera os bitmaps.
 */
class ProfilePhotoReader(
    private val resolver: ContentResolver,
    private val io: CoroutineDispatcher,
) : PhotoReader {
    override suspend fun read(reference: String): ByteArray =
        withContext(io) {
            try {
                val uri = Uri.parse(reference)
                require(uri.scheme == ContentResolver.SCHEME_CONTENT)
                prepareJpeg(readSource(uri))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                throw AuthException(AuthError.VALIDATION)
            }
        }

    /** Lê a URI selecionada em blocos e limita o tamanho da origem antes de decodificar. */
    private suspend fun readSource(uri: Uri): ByteArray =
        requireNotNull(resolver.openInputStream(uri)).use { input ->
            ByteArrayOutputStream().use { output ->
                val buffer = ByteArray(8192)
                var count = input.read(buffer)
                while (count != -1) {
                    // Arquivos maiores podem ser cancelados entre blocos, sem terminar toda a
                    // leitura.
                    currentCoroutineContext().ensureActive()
                    require(output.size() + count <= MAX_INPUT_BYTES)
                    output.write(buffer, 0, count)
                    count = input.read(buffer)
                }
                output.toByteArray()
            }
        }

    /**
     * Escolhe uma amostragem inicial para decodificar menos pixels; o tamanho final é ajustado
     * depois.
     */
    private fun decodeSampled(bytes: ByteArray): Bitmap {
        // Obtém dimensões sem criar o bitmap completo, reduzindo o pico de memória.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_EDGE * 2) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
    }

    /** Consulta a orientação do EXIF para que fotos de câmera não sejam enviadas giradas. */
    private fun readOrientation(bytes: ByteArray): Int =
        ByteArrayInputStream(bytes).use {
            ExifInterface(it)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }

    /** Combina rotação ou espelhamento com a escala necessária, sem ampliar imagens pequenas. */
    private fun photoTransform(bitmap: Bitmap, orientation: Int): Matrix =
        Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(270f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
            }
            val scale = minOf(1f, MAX_EDGE.toFloat() / maxOf(bitmap.width, bitmap.height))
            postScale(scale, scale)
        }

    /**
     * Coordena decodificação, transformação e codificação, liberando cada bitmap mesmo se uma etapa
     * falhar.
     */
    private fun prepareJpeg(bytes: ByteArray): ByteArray {
        val decoded = decodeSampled(bytes)
        try {
            val transform = photoTransform(decoded, readOrientation(bytes))
            val prepared =
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, transform, true)
            try {
                return encodeJpeg(prepared)
            } finally {
                if (prepared !== decoded) prepared.recycle()
            }
        } finally {
            decoded.recycle()
        }
    }

    /** Produz um novo arquivo JPEG e verifica o limite de bytes aceito pelo upload. */
    private fun encodeJpeg(bitmap: Bitmap): ByteArray =
        ByteArrayOutputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output))
            output.toByteArray().also { require(it.size <= MAX_UPLOAD_BYTES) }
        }

    companion object {
        const val MAX_EDGE = 1600
        const val MAX_INPUT_BYTES = 20 * 1024 * 1024
        const val MAX_UPLOAD_BYTES = 4 * 1024 * 1024
    }
}
