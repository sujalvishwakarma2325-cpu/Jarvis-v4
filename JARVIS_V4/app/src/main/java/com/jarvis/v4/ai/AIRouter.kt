package com.jarvis.v4.ai

class AIRouter(
    private val localAI: LocalAI
) {
    suspend fun ask(
        prompt: String
    ): Result<String> {
        return localAI.ask(prompt)
    }
}
