package com.jarvis.v4.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

data class DownloadProgress(
    val downloaded: Long,
    val total: Long,
    val speedBytesPerSecond: Long
) {
    val percent: Int
        get() {
            if (total <= 0L) return 0
            return ((downloaded * 100L) / total)
                .coerceIn(0L, 100L)
                .toInt()
        }
}

class ModelDownloader(
    private val context: Context
) {

    companion object {
        const val MODEL_NAME =
            "gemma-4-E2B-it.litertlm"

        private const val TEMP_NAME =
            "gemma-4-E2B-it.litertlm.part"

        private const val MODEL_DIRECTORY =
            "litert_models"

        private const val MODEL_URL =
            "https://huggingface.co/litert-community/" +
                "gemma-4-E2B-it-litert-lm/resolve/main/" +
                "gemma-4-E2B-it.litertlm"
    }

    private val directory: File
        get() = File(
            context.filesDir,
            MODEL_DIRECTORY
        )

    val modelFile: File
        get() = File(
            directory,
            MODEL_NAME
        )

    private val tempFile: File
        get() = File(
            directory,
            TEMP_NAME
        )

    fun isInstalled(): Boolean {
        return modelFile.exists() &&
            modelFile.length() > 0L
    }

    fun installedSize(): Long {
        return if (modelFile.exists()) {
            modelFile.length()
        } else {
            0L
        }
    }

    suspend fun download(
        onProgress: suspend (DownloadProgress) -> Unit
    ): Result<Unit> =
        withContext(Dispatchers.IO) {

            try {
                directory.mkdirs()

                if (isInstalled()) {
                    return@withContext Result.success(Unit)
                }

                var existingBytes =
                    if (tempFile.exists()) {
                        tempFile.length()
                    } else {
                        0L
                    }

                var connection =
                    openConnection(existingBytes)

                var responseCode =
                    connection.responseCode

                if (
                    existingBytes > 0L &&
                    responseCode == HttpURLConnection.HTTP_OK
                ) {
                    connection.disconnect()
                    tempFile.delete()
                    existingBytes = 0L
                    connection = openConnection(0L)
                    responseCode = connection.responseCode
                }

                if (
                    responseCode !=
                    HttpURLConnection.HTTP_OK &&
                    responseCode !=
                    HttpURLConnection.HTTP_PARTIAL
                ) {
                    connection.disconnect()

                    return@withContext Result.failure(
                        Exception(
                            "Download server returned HTTP " +
                                responseCode
                        )
                    )
                }

                val contentLength =
                    connection.getHeaderFieldLong(
                        "Content-Length",
                        -1L
                    )

                val totalBytes =
                    if (
                        responseCode ==
                            HttpURLConnection.HTTP_PARTIAL
                    ) {
                        existingBytes + contentLength
                    } else {
                        contentLength
                    }

                val input =
                    connection.inputStream

                val output =
                    RandomAccessFile(
                        tempFile,
                        "rw"
                    )

                if (existingBytes > 0L) {
                    output.seek(existingBytes)
                } else {
                    output.setLength(0L)
                }

                val buffer = ByteArray(1024 * 1024)

                var downloaded = existingBytes
                var lastTime = System.currentTimeMillis()
                var lastBytes = downloaded

                while (true) {

                    val count =
                        input.read(buffer)

                    if (count == -1) {
                        break
                    }

                    output.write(
                        buffer,
                        0,
                        count
                    )

                    downloaded += count

                    val now =
                        System.currentTimeMillis()

                    if (
                        now - lastTime >= 500L
                    ) {
                        val elapsed =
                            now - lastTime

                        val speed =
                            if (elapsed > 0L) {
                                (
                                    (downloaded -
                                        lastBytes) *
                                        1000L
                                ) / elapsed
                            } else {
                                0L
                            }

                        onProgress(
                            DownloadProgress(
                                downloaded = downloaded,
                                total = totalBytes,
                                speedBytesPerSecond = speed
                            )
                        )

                        lastTime = now
                        lastBytes = downloaded
                    }
                }

                output.close()
                input.close()
                connection.disconnect()

                if (
                    totalBytes > 0L &&
                    downloaded < totalBytes
                ) {
                    return@withContext Result.failure(
                        Exception(
                            "Download ended before completion."
                        )
                    )
                }

                if (downloaded <= 0L) {
                    return@withContext Result.failure(
                        Exception(
                            "Downloaded model is empty."
                        )
                    )
                }

                if (modelFile.exists()) {
                    modelFile.delete()
                }

                if (!tempFile.renameTo(modelFile)) {
                    return@withContext Result.failure(
                        Exception(
                            "Could not install downloaded model."
                        )
                    )
                }

                onProgress(
                    DownloadProgress(
                        downloaded = downloaded,
                        total = downloaded,
                        speedBytesPerSecond = 0L
                    )
                )

                Result.success(Unit)

            } catch (e: Exception) {

                Result.failure(
                    Exception(
                        e.message
                            ?: "Model download failed."
                    )
                )
            }
        }

    private fun openConnection(
        startByte: Long
    ): HttpURLConnection {

        val connection =
            URL(MODEL_URL)
                .openConnection()
                as HttpURLConnection

        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true

        if (startByte > 0L) {
            connection.setRequestProperty(
                "Range",
                "bytes=$startByte-"
            )
        }

        connection.connect()

        return connection
    }

    fun deletePartialDownload() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
