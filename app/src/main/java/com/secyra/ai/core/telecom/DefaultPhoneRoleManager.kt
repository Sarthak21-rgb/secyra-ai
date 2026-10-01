package com.secyra.ai.core.telecom

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the default phone app role request and status.
 * 
 * For Secyra to provide its own in-call UI, it must be set as the default phone app
 * through Android's RoleManager (ROLE_DIALER).
 * 
 * This is NOT automatic - the user must explicitly approve this in Android settings.
 */
@Singleton
class DefaultPhoneRoleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val roleManager: RoleManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    } else {
        null
    }

    private val _isDefaultPhoneApp = MutableStateFlow(false)
    val isDefaultPhoneApp: StateFlow<Boolean> = _isDefaultPhoneApp.asStateFlow()

    /**
     * Check if Secyra is currently the default phone app
     */
    fun checkDefaultPhoneStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            _isDefaultPhoneApp.value = roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
        } else {
            // On older Android versions, check through other means
            _isDefaultPhoneApp.value = false
        }
    }

    /**
     * Create an intent to request the default phone app role
     * This will show Android's system dialog asking the user to set Secyra as default
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createRequestRoleIntent(): Intent? {
        return roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)
    }

    /**
     * Check if the role request is available
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isRoleRequestAvailable(): Boolean {
        return roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true
    }

    companion object {
        const val REQUEST_CODE_DEFAULT_PHONE = 1001
    }
}
