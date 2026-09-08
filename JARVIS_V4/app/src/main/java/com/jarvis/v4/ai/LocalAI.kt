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

    companion object {
        private const val MODEL_NAME =
            "gemma-4-E2B-it.litertlm"

        private const val MODEL_DIRECTORY =
            "litert_models"
    }

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var initialized = false

    private val modelFile: File
        get() = File(
            File(
                context.filesDir,
                MODEL_DIRECTORY
            ),
            MODEL_NAME
        )

    suspend fun initialize(): Result<String> =
        withContext(Dispatchers.IO) {

            if (initialized) {
                return@withContext Result.success(
                    "Local Gemma is already ready."
                )
            }

            if (!modelFile.exists()) {
                return@withContext Result.failure(
                    Exception(
                        "Gemma model is not installed."
                    )
                )
            }

            try {

                val config = EngineConfig(
                    modelPath =
                        modelFile.absolutePath,
                    backend = Backend.CPU()
                )

                val newEngine =
                    Engine(config)

                newEngine.initialize()

                engine = newEngine

                conversation =
                    newEngine.createConversation()

                initialized = true

                Result.success(
                    "Local Gemma is ready."
                )

            } catch (e: Exception) {

                try {
                    engine?.close()
                } catch (_: Exception) {
                }

                engine = null
                conversation = null
                initialized = false

                Result.failure(
                    Exception(
                        "Local AI initialization failed: " +
                            (
                                e.message
                                    ?: "Unknown error"
                            )
                    )
                )
            }
        }

    suspend fun ask(
        prompt: String
    ): Result<String> =
        withContext(Dispatchers.IO) {

            if (prompt.isBlank()) {
                return@withContext Result.failure(
                    Exception("Prompt is empty.")
                )
            }

            if (!initialized) {
                val result = initialize()

                if (result.isFailure) {
                    return@withContext Result.failure(
                        result.exceptionOrNull()
                            ?: Exception(
                                "Local AI initialization failed."
                            )
                    )
                }
            }

            try {

                val currentConversation =
                    conversation
                        ?: return@withContext Result.failure(
                            Exception(
                                "Conversation is unavailable."
                            )
                        )

                val fullPrompt = """
                    You are JARVIS, a personal AI assistant.

                    Answer naturally and concisely.

                    The user may speak English,
                    Hindi, or Hinglish.

                    Keep answers suitable
                    for a voice assistant.

                    User:
                    $prompt
                """.trimIndent()

                val response =
                    StringBuilder()

                currentConversation
                    .sendMessageAsync(fullPrompt)
                    .collect { message ->
                        response.append(
                            message.toString()
                        )
                    }

                val answer =
                    response.toString().trim()

                if (answer.isBlank()) {
                    return@withContext Result.failure(
                        Exception(
                            "Local Gemma returned an empty response."
                        )
                    )
                }

                Result.success(answer)

            } catch (e: Exception) {

                Result.failure(
                    Exception(
                        "Local AI error: " +
                            (
                                e.message
                                    ?: "Unknown error"
                            )
                  )
                )
            }
        }

    fun isModelInstalled(): Boolean {
        return modelFile.exists()
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

        conversation = null
        engine = null
        initialized = false
    }
}
