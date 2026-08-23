package com.example.aprendeaprender.data.ai

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

class GemmaModelDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val modelUrl = inputData.getString(GemmaModelConstants.KEY_MODEL_URL).orEmpty()
        val token = inputData.getString(GemmaModelConstants.KEY_MODEL_TOKEN).orEmpty()
        val expectedSha256 = inputData.getString(GemmaModelConstants.KEY_EXPECTED_SHA256).orEmpty()
        val expectedSizeBytes = inputData.getLong(GemmaModelConstants.KEY_EXPECTED_SIZE_BYTES, 0L)

        if (modelUrl.isBlank()) {
            return@withContext failure("No se configuró GEMMA_MODEL_URL.")
        }

        val modelsDir = File(applicationContext.filesDir, GemmaModelConstants.MODELS_DIRECTORY)
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }

        val tempFile = File(modelsDir, GemmaModelConstants.TEMP_MODEL_FILE_NAME)
        val modelFile = File(modelsDir, GemmaModelConstants.MODEL_FILE_NAME)

        if (modelFile.exists() && modelFile.length() > 0L) {
            val sizeOk = expectedSizeBytes <= 0L || modelFile.length() == expectedSizeBytes

            if (sizeOk) {
                setProgress(
                    Data.Builder()
                        .putInt(GemmaModelConstants.KEY_PROGRESS, 100)
                        .putLong(GemmaModelConstants.KEY_DOWNLOADED_BYTES, modelFile.length())
                        .putLong(GemmaModelConstants.KEY_TOTAL_BYTES, modelFile.length())
                        .build()
                )

                return@withContext Result.success()
            }

            modelFile.delete()
        }

        if (expectedSizeBytes > 0L && tempFile.exists() && tempFile.length() > expectedSizeBytes) {
            Log.d(TAG, "Temporal más grande que el esperado. Se elimina.")
            tempFile.delete()
        }

        if (
            expectedSizeBytes > 0L &&
            tempFile.exists() &&
            tempFile.length() == expectedSizeBytes
        ) {
            return@withContext finalizarTemporal(
                tempFile = tempFile,
                modelFile = modelFile,
                expectedSha256 = expectedSha256,
                expectedSizeBytes = expectedSizeBytes
            )
        }

        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        try {
            var bytesYaDescargados = if (tempFile.exists()) tempFile.length() else 0L

            val requestBuilder = Request.Builder()
                .url(modelUrl)
                .addHeader("User-Agent", "Mozilla/5.0 AprendeAprenderAndroid/1.0")
                .addHeader("Accept", "application/octet-stream,*/*")

            if (token.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            if (bytesYaDescargados > 0L) {
                requestBuilder.addHeader("Range", "bytes=$bytesYaDescargados-")
                Log.d(TAG, "Reanudando descarga desde byte $bytesYaDescargados")
            } else {
                Log.d(TAG, "Iniciando descarga desde cero.")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                Log.d(TAG, "HTTP code = ${response.code}")
                Log.d(TAG, "Content-Type = ${response.header("Content-Type")}")
                Log.d(TAG, "Content-Length = ${response.body?.contentLength()}")
                Log.d(TAG, "Content-Range = ${response.header("Content-Range")}")

                if (response.code == 416) {
                    if (
                        expectedSizeBytes > 0L &&
                        tempFile.exists() &&
                        tempFile.length() == expectedSizeBytes
                    ) {
                        return@withContext finalizarTemporal(
                            tempFile = tempFile,
                            modelFile = modelFile,
                            expectedSha256 = expectedSha256,
                            expectedSizeBytes = expectedSizeBytes
                        )
                    }

                    tempFile.delete()
                    return@withContext Result.retry()
                }

                if (!response.isSuccessful) {
                    return@withContext if (esErrorTemporal(response.code)) {
                        Result.retry()
                    } else {
                        failure("No se pudo descargar el modelo. HTTP ${response.code}.")
                    }
                }

                val contentType = response.header("Content-Type").orEmpty()

                if (contentType.contains("text/html", ignoreCase = true)) {
                    return@withContext failure(
                        "La URL devolvió HTML, no el modelo. Revisa si usaste /blob/ en vez de /resolve/."
                    )
                }

                val servidorAceptoRange = bytesYaDescargados > 0L && response.code == 206
                val servidorIgnoroRange = bytesYaDescargados > 0L && response.code == 200

                if (servidorIgnoroRange) {
                    Log.d(TAG, "El servidor ignoró Range. Reiniciando desde cero.")
                    tempFile.delete()
                    bytesYaDescargados = 0L
                }

                val append = servidorAceptoRange
                val body = response.body ?: return@withContext Result.retry()

                val totalBytes = obtenerTotalBytes(
                    response = response,
                    bytesYaDescargados = bytesYaDescargados,
                    contentLength = body.contentLength(),
                    expectedSizeBytes = expectedSizeBytes
                )

                body.byteStream().use { input ->
                    FileOutputStream(tempFile, append).use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        val startTime = System.currentTimeMillis()
                        var totalRead = bytesYaDescargados
                        var lastLoggedMb = totalRead / BYTES_PER_MB
                        var lastProgress = -1

                        publicarProgreso(totalRead, totalBytes)

                        while (true) {
                            if (isStopped) {
                                Log.d(TAG, "Worker detenido. Se conserva temporal para reanudar.")
                                return@withContext Result.retry()
                            }

                            val read = input.read(buffer)

                            if (read == -1) {
                                break
                            }

                            output.write(buffer, 0, read)
                            totalRead += read

                            val currentMb = totalRead / BYTES_PER_MB

                            if (currentMb >= lastLoggedMb + 10L) {
                                lastLoggedMb = currentMb

                                val seconds = ((System.currentTimeMillis() - startTime) / 1000.0)
                                    .coerceAtLeast(1.0)

                                val mbPerSecond = currentMb / seconds

                                Log.d(
                                    TAG,
                                    "Descargados: $currentMb MB | Velocidad: ${
                                        String.format(Locale.US, "%.2f", mbPerSecond)
                                    } MB/s"
                                )
                            }

                            val progress = calcularProgreso(totalRead, totalBytes)

                            if (progress != lastProgress) {
                                lastProgress = progress
                                publicarProgreso(totalRead, totalBytes)
                            }
                        }

                        output.flush()
                    }
                }
            }

            finalizarTemporal(
                tempFile = tempFile,
                modelFile = modelFile,
                expectedSha256 = expectedSha256,
                expectedSizeBytes = expectedSizeBytes
            )
        } catch (exception: IOException) {
            Log.e(TAG, "Error de red. Se conserva temporal para reintentar.", exception)
            Result.retry()
        } catch (exception: Exception) {
            Log.e(TAG, "Error descargando modelo.", exception)
            failure(exception.message ?: "Error descargando el modelo.")
        }
    }

    private fun finalizarTemporal(
        tempFile: File,
        modelFile: File,
        expectedSha256: String,
        expectedSizeBytes: Long
    ): Result {
        if (!tempFile.exists() || tempFile.length() <= 0L) {
            return failure("El archivo descargado está vacío.")
        }

        if (expectedSizeBytes > 0L && tempFile.length() != expectedSizeBytes) {
            val receivedSize = tempFile.length()
            tempFile.delete()

            return failure(
                "El tamaño del modelo no coincide. Esperado: $expectedSizeBytes bytes. Recibido: $receivedSize bytes."
            )
        }

        if (expectedSha256.isNotBlank()) {
            val actualSha256 = sha256(tempFile)

            if (!actualSha256.equals(expectedSha256.trim(), ignoreCase = true)) {
                tempFile.delete()
                return failure("El hash SHA-256 del modelo no coincide.")
            }
        }

        if (modelFile.exists()) {
            modelFile.delete()
        }

        val renamed = tempFile.renameTo(modelFile)

        if (!renamed) {
            tempFile.copyTo(modelFile, overwrite = true)
            tempFile.delete()
        }

        if (!modelFile.exists() || modelFile.length() <= 0L) {
            return failure("No fue posible guardar el modelo final.")
        }

        publicarProgreso(modelFile.length(), modelFile.length())

        Log.d(TAG, "Modelo guardado correctamente en: ${modelFile.absolutePath}")

        return Result.success()
    }

    private fun obtenerTotalBytes(
        response: Response,
        bytesYaDescargados: Long,
        contentLength: Long,
        expectedSizeBytes: Long
    ): Long {
        if (expectedSizeBytes > 0L) {
            return expectedSizeBytes
        }

        val contentRange = response.header("Content-Range")

        if (contentRange != null && contentRange.contains("/")) {
            val total = contentRange.substringAfter("/").toLongOrNull()
            if (total != null && total > 0L) {
                return total
            }
        }

        return if (contentLength > 0L) {
            bytesYaDescargados + contentLength
        } else {
            0L
        }
    }

    private fun calcularProgreso(
        downloadedBytes: Long,
        totalBytes: Long
    ): Int {
        if (totalBytes <= 0L) return 0

        return ((downloadedBytes * 100L) / totalBytes)
            .coerceIn(0L, 99L)
            .toInt()
    }

    private fun publicarProgreso(
        downloadedBytes: Long,
        totalBytes: Long
    ) {
        val progress = if (downloadedBytes >= totalBytes && totalBytes > 0L) {
            100
        } else {
            calcularProgreso(downloadedBytes, totalBytes)
        }

        val data = Data.Builder()
            .putInt(GemmaModelConstants.KEY_PROGRESS, progress)
            .putLong(GemmaModelConstants.KEY_DOWNLOADED_BYTES, downloadedBytes)
            .putLong(GemmaModelConstants.KEY_TOTAL_BYTES, totalBytes)
            .build()

        setProgressAsync(data)
    }

    private fun esErrorTemporal(code: Int): Boolean {
        return code in 500..599 || code == 408 || code == 429
    }

    private fun failure(message: String): Result {
        Log.e(TAG, message)

        return Result.failure(
            Data.Builder()
                .putString(GemmaModelConstants.KEY_ERROR, message)
                .build()
        )
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")

        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)

            while (true) {
                val read = input.read(buffer)

                if (read == -1) break

                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().joinToString("") { byte ->
            "%02x".format(byte)
        }
    }

    private companion object {
        const val TAG = "GemmaDownload"
        const val BUFFER_SIZE = 1024 * 1024
        const val BYTES_PER_MB = 1024L * 1024L
    }
}