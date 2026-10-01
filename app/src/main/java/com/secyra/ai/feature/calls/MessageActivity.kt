package com.secyra.ai.feature.calls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.secyra.ai.core.ui.theme.SecyraTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for messaging the caller.
 */
@AndroidEntryPoint
class MessageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SecyraTheme {
                MessageScreen(
                    recipientName = "Unknown",
                    recipientNumber = "",
                    onClose = { finish() },
                    modifier = Modifier
                )
            }
        }
    }
}
