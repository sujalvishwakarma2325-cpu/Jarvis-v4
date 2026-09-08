package com.jarvis.v4.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.io.File

class LocalAI(
    private val context: Context
) {

    private var engine: Engine? =
        null

    private var conversation:
        Conversation? = null

    private val modelFile: File
        get() = File(
            File(
                context.filesDir,
                "litert_models"
            ),
            ModelDownloader.MODEL_NAME
        )

    suspend fun initialize():
        Result<String> =
        withContext(Dispatchers.IO) {

            if (!modelFile.exists()) {

                return@withContext Result.failure(
                    Exception(
                        "Gemma model is not installed."
                    )
                )
            }

            try {

                val newEngine =
                    Engine(
                        EngineConfig(
                            modelPath =
                                modelFile.absolutePath,
                            backend =
                                Backend.CPU()
                        )
                    )

                newEngine.initialize()

                engine =
                    newEngine

                conversation =
                    newEngine
                        .createConversation()

                Result.success(
                    "Local Gemma is ready."
                )

            } catch (e: Exception) {

                close()

                Result.failure(
                    Exception(
                        e.message
                            ?: "Local AI failed."
                    )
                )
            }
        }

    suspend fun ask(
        prompt: String
    ): Result<String> =
        withContext(Dispatchers.IO) {

            if (conversation == null) {

                val init =
                    initialize()

                if (init.isFailure) {

                    return@withContext Result.failure(
                        init.exceptionOrNull()
                            ?: Exception(
                                "Local AI failed."
                            )
                    )
                }
            }

            try {

                val output =
                    StringBuilder()

                conversation!!
                    .sendMessageAsync(prompt)
                    .collect { message ->

                        output.append(
                            message.toString()
                        )
                    }

                Result.success(
                    output.toString().trim()
                )

            } catch (e: Exception) {

                Result.failure(
                    Exception(
                        e.message
                            ?: "Local AI error."
                    )
                )
            }
        }

    fun close() {

        try {
            conversation?.close()
        } catch (_: Exception) {
        }

        try {
            engine?.close()
        } catch (_: Exception) {
        }

        conversation =
            null

        engine =
            null
    }
}
