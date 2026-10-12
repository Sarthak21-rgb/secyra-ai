package com.secyra.ai.core.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * AIProvider abstraction for AI-powered security analysis.
 * 
 * This is designed to work with a secure backend architecture.
 * Current implementation: Llama 3.1 8B Instruct via Supabase Edge Functions
 * 
 * IMPORTANT: Never place private AI API keys in the Android app.
 * Use a secure backend service to handle AI communication.
 */
@Singleton
class AIProvider @Inject constructor(
    private val llamaProvider: LlamaProvider
) {

    data class AIAnalysisRequest(
        val content: String,
        val context: Map<String, String> = emptyMap(),
        val securityContext: SecurityContext? = null
    )

    data class SecurityContext(
        val url: String? = null,
        val phoneNumber: String? = null,
        val message: String? = null,
        val qrData: String? = null,
        val metadata: Map<String, String> = emptyMap()
    )

    data class AIAnalysisResponse(
        val explanation: String,
        val riskAssessment: String,
        val recommendations: List<String>,
        val confidence: Float,
        val uncertainty: String? = null,
        val sources: List<String> = emptyList()
    )

    data class AIChatMessage(
        val role: String, // "user", "assistant", "system"
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Analyze security content using AI
     * This communicates with Supabase Edge Function → Together AI → Llama 3.1 8B
     */
    suspend fun analyzeSecurity(request: AIAnalysisRequest): Result<AIAnalysisResponse> {
        return llamaProvider.analyzeSecurity(request)
    }

    /**
     * Generate a security explanation for analysis results
     */
    suspend fun generateExplanation(
        securityAnalysis: SecurityEngine.SecurityAnalysis,
        context: SecurityContext
    ): Result<String> {
        return try {
            val prompt = buildString {
                append("Explain this security analysis in simple terms:\n")
                append("Risk Level: ${securityAnalysis.riskLevel}\n")
                append("Confidence: ${securityAnalysis.confidence}\n")
                append("Evidence: ${securityAnalysis.evidence.joinToString(", ")}\n")
                append("Reason: ${securityAnalysis.reason}\n")
                append("Make it easy for a non-technical user to understand.")
            }

            val response = llamaProvider.chatWithAI(
                conversation = emptyList(),
                newMessage = prompt
            )

            response.map { it.content }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Chat with AI security assistant
     * Uses Llama 3.1 8B Instruct with cybersecurity-focused system prompt
     */
    suspend fun chatWithAI(
        conversation: List<AIChatMessage>,
        newMessage: String
    ): Result<AIChatMessage> {
        return llamaProvider.chatWithAI(conversation, newMessage)
    }

    /**
     * Analyze an image/screenshot for security risks
     * NOTE: Llama 3.1 8B is text-only. For image analysis, upgrade to Llama 4 Maverick.
     */
    suspend fun analyzeImage(
        imageData: ByteArray,
        context: SecurityContext
    ): Result<AIAnalysisResponse> {
        return Result.failure(
            NotImplementedError("Image analysis requires multimodal model (Llama 4 Maverick)")
        )
    }

    /**
     * Voice-to-text for security queries
     * NOTE: Requires separate speech-to-text service
     */
    suspend fun transcribeVoice(audioData: ByteArray): Result<String> {
        return Result.failure(
            NotImplementedError("Voice transcription requires separate STT service")
        )
    }

    /**
     * Text-to-speech for AI responses
     * NOTE: Requires separate TTS service
     */
    suspend fun synthesizeSpeech(text: String): Result<ByteArray> {
        return Result.failure(
            NotImplementedError("Speech synthesis requires separate TTS service")
        )
    }

    /**
     * Check if AI service is available
     */
    suspend fun isAvailable(): Boolean {
        return llamaProvider.isAvailable()
    }
}
