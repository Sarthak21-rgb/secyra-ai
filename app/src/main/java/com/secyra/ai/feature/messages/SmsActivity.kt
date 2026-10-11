package com.secyra.ai.feature.messages

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.secyra.ai.core.ui.theme.SecyraTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for handling SMS-related intents.
 * This is required for Secyra to function as a default SMS app.
 */
@AndroidEntryPoint
class SmsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle incoming SMS intent
        val action = intent.action
        val data = intent.data

        setContent {
            SecyraTheme {
                // For now, just show a placeholder
                // In a full implementation, this would show the conversation
                // or compose screen based on the intent
            }
        }
    }
}
