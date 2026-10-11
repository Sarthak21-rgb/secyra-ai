package com.secyra.ai.core.telecom

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling the default SMS app role.
 * Uses Android's official RoleManager.ROLE_SMS.
 */
@Singleton
class SmsRoleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Check if Secyra is currently the default SMS app.
     */
    fun isDefaultSmsApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            // Fallback for older Android versions
            // Pre-Q, check if app is in the list of default SMS apps
            false
        }
    }

    /**
     * Check if the SMS role is available for request.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isSmsRoleAvailable(): Boolean {
        val roleManager = context.getSystemService(RoleManager::class.java)
        return roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true
    }

    /**
     * Create an intent to request the SMS role.
     */
    fun createRequestRoleIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            // For older Android versions, direct the user to default app settings
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
        }
    }

    /**
     * Check default SMS status and trigger callbacks.
     */
    fun checkDefaultSmsStatus() {
        // This can be expanded to trigger UI updates when role status changes
        val isDefault = isDefaultSmsApp()
        // Notify listeners or update state as needed
    }
}
