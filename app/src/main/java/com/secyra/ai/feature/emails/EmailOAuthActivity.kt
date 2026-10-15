package com.secyra.ai.feature.emails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.secyra.ai.core.ui.theme.SecyraWhite
import kotlinx.coroutines.launch

/**
 * Activity for handling OAuth callbacks from Gmail and Outlook.
 * This activity receives the OAuth redirect and passes the authorization code to the view model.
 */
class EmailOAuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data
        val authCode = data?.getQueryParameter("code")
        val error = data?.getQueryParameter("error")

        setContent {
            OAuthCallbackScreen(
                isLoading = authCode != null && error == null,
                error = error,
                onSuccess = {
                    // Return success to the caller
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("auth_code", authCode)
                        putExtra("provider", data?.getQueryParameter("provider"))
                    })
                    finish()
                },
                onError = {
                    // Return error to the caller
                    setResult(RESULT_CANCELED, Intent().apply {
                        putExtra("error", error)
                    })
                    finish()
                }
            )
        }

        // Automatically process the callback
        lifecycleScope.launch {
            if (error != null) {
                // Error in OAuth flow
                return@launch
            }

            if (authCode != null) {
                // Success - will be handled by the composable
            } else {
                // No code received - might be initial launch
                finish()
            }
        }
    }
}

@Composable
fun OAuthCallbackScreen(
    isLoading: Boolean,
    error: String?,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = com.secyra.ai.core.ui.theme.TiffanyBlue)
                    Text(
                        text = "Connecting your email account...",
                        color = SecyraWhite
                    )
                }
            }
            error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Authentication Failed",
                        color = com.secyra.ai.core.ui.theme.RiskHigh,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = error,
                        color = com.secyra.ai.core.ui.theme.SecyraTextSecondary
                    )
                }
                LaunchedEffect(error) {
                    onError()
                }
            }
            else -> {
                Text(
                    text = "Processing...",
                    color = SecyraWhite
                )
            }
        }
    }
}
