package com.secyra.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.secyra.ai.core.telecom.PhoneAccountRegistrar
import com.secyra.ai.core.ui.theme.SecyraTheme
import com.secyra.ai.navigation.SecyraNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var phoneAccountRegistrar: PhoneAccountRegistrar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register Secyra's PhoneAccount with Android Telecom
        // This makes Secyra appear as a candidate for default phone app
        try {
            phoneAccountRegistrar.registerPhoneAccount()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        enableEdgeToEdge()
        setContent {
            SecyraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SecyraNavHost()
                }
            }
        }
    }
}
