package com.jarvis.v4.ai

class ClaudeAI : AIProvider {

    override suspend fun ask(
        prompt: String
    ): Result<String> {

        return Result.failure(
            Exception(
                "Claude provider not configured yet."
            )
        )
    }
}
