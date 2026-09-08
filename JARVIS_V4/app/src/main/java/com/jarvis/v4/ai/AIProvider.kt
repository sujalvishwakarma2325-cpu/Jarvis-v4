package com.jarvis.v4.ai

interface AIProvider {
    suspend fun ask(
        prompt: String
    ): Result<String>
}
