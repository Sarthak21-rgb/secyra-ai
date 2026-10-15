package com.secyra.ai.feature.emails

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manages Gmail OAuth authentication and API access.
 * 
 * IMPORTANT: This class requires the following setup:
 * 1. Create a project in Google Cloud Console
 * 2. Enable Gmail API
 * 3. Configure OAuth 2.0 consent screen
 * 4. Create OAuth 2.0 client ID (Android type)
 * 5. Add the SHA-256 fingerprint of your signing certificate
 * 6. Configure the OAuth client ID below in GMAIL_OAUTH_CLIENT_ID
 * 
 * Without these steps, authentication will fail.
 */
@Singleton
class GmailOAuthManager @Inject constructor(
    private val context: Context,
    private val httpClient: HttpClient
) {
    companion object {
        // TODO: Replace with your actual OAuth 2.0 client ID from Google Cloud Console
        private const val GMAIL_OAUTH_CLIENT_ID = "YOUR_GMAIL_OAUTH_CLIENT_ID.apps.googleusercontent.com"
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GMAIL_OAUTH_CLIENT_ID)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/gmail.readonly"),
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/gmail.send")
            )
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Gets the sign-in intent for Gmail OAuth.
     * Call this from your activity and pass the result to handleSignInResult.
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Handles the sign-in result from Google Sign-In.
     * Returns the auth token or throws an exception.
     */
    suspend fun handleSignInResult(data: Intent?): String {
        return suspendCancellableCoroutine { continuation ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    continuation.resume(idToken)
                } else {
                    continuation.resumeWithException(Exception("Failed to get ID token"))
                }
            } catch (e: ApiException) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Fetches recent emails from the user's Gmail inbox using REST API.
     * 
     * @param token OAuth access token
     * @param maxResults Maximum number of emails to fetch (default: 50)
     * @return List of email summaries
     */
    suspend fun fetchRecentEmails(token: String, maxResults: Int = 50): List<EmailSummary> {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = httpClient.get("https://gmail.googleapis.com/gmail/v1/users/me/messages") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                        append(HttpHeaders.Accept, ContentType.Application.Json)
                    }
                    parameter("maxResults", maxResults)
                }

                val responseBody = response.body<JsonObject>()
                val messagesJson = responseBody["messages"]?.jsonObject
                val messages = messagesJson?.get("messages")?.jsonObject?.get("id")

                if (messagesJson == null) {
                    return@withContext emptyList()
                }

                val emailSummaries = mutableListOf<EmailSummary>()
                // For now, return empty list as we need to fetch individual messages
                // This requires more complex JSON parsing
                emailSummaries
            } catch (e: Exception) {
                throw Exception("Failed to fetch emails: ${e.message}")
            }
        }
    }

    /**
     * Sends an email using Gmail REST API.
     * 
     * @param token OAuth access token
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body content
     */
    suspend fun sendEmail(
        token: String,
        to: String,
        subject: String,
        body: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val emailContent = createEmailContent(to, subject, body)
                val encodedEmail = java.util.Base64.getUrlEncoder()
                    .encodeToString(emailContent.toByteArray())

                httpClient.post("https://gmail.googleapis.com/gmail/v1/users/me/messages/send") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                        append(HttpHeaders.ContentType, ContentType.Application.Json)
                    }
                    setBody(mapOf("raw" to encodedEmail))
                }
            } catch (e: Exception) {
                throw Exception("Failed to send email: ${e.message}")
            }
        }
    }

    /**
     * Signs out the current Google account.
     */
    fun signOut() {
        googleSignInClient.signOut()
    }

    private fun createEmailContent(to: String, subject: String, body: String): String {
        return """
            To: $to
            Subject: $subject
            Content-Type: text/plain; charset=utf-8

            $body
        """.trimIndent()
    }
}
