package com.secyra.ai.core.security

import com.secyra.ai.core.network.SupabaseConfig
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Llama 3.1 8B Instruct provider implementation using Supabase Edge Functions.
 * 
 * Model: Meta Llama 3.1 8B Instruct
 * Provider: Together AI (via Supabase Edge Function)
 * Model ID: nim/meta/llama-3-1-8b-instruct
 * 
 * Architecture: Android → Supabase Edge Function → Together AI → Llama 3.1 → Response
 * 
 * Security: API key stored in Supabase Edge Function environment variables, never in the app.
 */
@Singleton
class LlamaProvider @Inject constructor(
    private val supabaseConfig: SupabaseConfig
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation)
        install(Logging) {
            level = io.ktor.client.plugins.logging.LogLevel.NONE
        }
    }

    /**
     * Chat request data class
     */
    @Serializable
    data class ChatRequest(
        val messages: List<ChatMessage>,
        val model: String = "nim/meta/llama-3-1-8b-instruct",
        val max_tokens: Int = 1000,
        val temperature: Float = 0.7f,
        val stream: Boolean = false
    )

    @Serializable
    data class ChatMessage(
        val role: String,
        val content: String
    )

    /**
     * Chat response data class
     */
    @Serializable
    data class ChatResponse(
        val id: String,
        val choices: List<Choice>
    )

    @Serializable
    data class Choice(
        val index: Int,
        val message: ChatMessage
    )

    /**
     * Security analysis request with structured output
     */
    @Serializable
    data class SecurityAnalysisRequest(
        val content: String,
        val analysis_type: String,
        val context: Map<String, String> = emptyMap()
    )

    /**
     * Structured security analysis response
     */
    @Serializable
    data class SecurityAnalysisResponse(
        val risk_level: String, // low, medium, high, unknown
        val confidence: Float,
        val summary: String,
        val evidence: List<String>,
        val recommended_actions: List<String>,
        val limitations: List<String>
    )

    /**
     * Send chat request to Llama via Supabase Edge Function
     */
    suspend fun chatWithAI(
        conversation: List<AIProvider.AIChatMessage>,
        newMessage: String
    ): Result<AIProvider.AIChatMessage> {
        return try {
            val messages = conversation.map { 
                ChatMessage(role = it.role, content = it.content) 
            } + ChatMessage(role = "user", content = newMessage)

            val request = ChatRequest(messages = messages)

            val response = httpClient.post(supabaseConfig.edgeFunctionChat) {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }

            val responseBody = response.body<ChatResponse>()
            val assistantMessage = responseBody.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI"))

            Result.success(
                AIProvider.AIChatMessage(
                    role = "assistant",
                    content = assistantMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze security content with structured output
     */
    suspend fun analyzeSecurity(
        request: AIProvider.AIAnalysisRequest
    ): Result<AIProvider.AIAnalysisResponse> {
        return try {
            val securityRequest = SecurityAnalysisRequest(
                content = request.content,
                analysis_type = when {
                    request.securityContext?.url != null -> "url_analysis"
                    request.securityContext?.phoneNumber != null -> "phone_analysis"
                    request.securityContext?.message != null -> "message_analysis"
                    else -> "general_security"
                },
                context = mapOf(
                    "url" to (request.securityContext?.url ?: ""),
                    "phone" to (request.securityContext?.phoneNumber ?: ""),
                    "message" to (request.securityContext?.message ?: "")
                )
            )

            val response = httpClient.post(supabaseConfig.edgeFunctionAnalyze) {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(json.encodeToString(securityRequest))
            }

            val responseBody = response.body<SecurityAnalysisResponse>()

            Result.success(
                AIProvider.AIAnalysisResponse(
                    explanation = responseBody.summary,
                    riskAssessment = responseBody.risk_level,
                    recommendations = responseBody.recommended_actions,
                    confidence = responseBody.confidence,
                    uncertainty = if (responseBody.limitations.isNotEmpty()) 
                        responseBody.limitations.joinToString(", ") 
                    else null,
                    sources = responseBody.evidence
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if provider is available
     */
    suspend fun isAvailable(): Boolean {
        return try {
            val response = httpClient.post(supabaseConfig.edgeFunctionChat) {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(json.encodeToString(
                    ChatRequest(messages = listOf(ChatMessage(role = "user", content = "test")))
                ))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }
}
