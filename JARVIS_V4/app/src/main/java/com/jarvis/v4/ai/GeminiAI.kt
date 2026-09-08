package com.jarvis.v4.ai

class GeminiAI : AIProvider {

    override suspend fun ask(
        prompt: String
    ): Result<String> {

        return Result.failure(
            Exception(
                "Gemini provider not configured yet."
            )
        )
    }
}
