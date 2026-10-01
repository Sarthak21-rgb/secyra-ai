package com.secyra.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SecyraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // PhoneAccount registration will be handled in a separate lifecycle-aware component
        // to avoid issues with Hilt injection timing
    }
}
