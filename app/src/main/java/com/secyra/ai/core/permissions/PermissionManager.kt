package com.secyra.ai.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Permission manager for SECYRA AI.
 * 
 * IMPORTANT:
 * - Only request permissions that are genuinely required
 * - Request permissions only when needed
 * - Follow current Android permission policies
 * - Never request every permission at startup
 * - Respect user's privacy choices
 */
@Singleton
class PermissionManager @Inject constructor(
    private val context: Context
) {

    /**
     * Check if a permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check multiple permissions
     */
    fun arePermissionsGranted(permissions: List<String>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }

    /**
     * Check if we need to request runtime permissions
     */
    fun shouldRequestPermission(permission: String): Boolean {
        return !isPermissionGranted(permission)
    }

    /**
     * Common permission groups used by SECYRA AI
     */
    object Permissions {
        // Camera - for QR scanning
        val CAMERA = Manifest.permission.CAMERA

        // Internet - for API calls
        val INTERNET = Manifest.permission.INTERNET
        val NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE

        // Storage - for file analysis
        val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
        val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO

        // Phone - for call analysis
        val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        val READ_CALL_LOG = Manifest.permission.READ_CALL_LOG
        val READ_CONTACTS = Manifest.permission.READ_CONTACTS

        // SMS - for message analysis
        val READ_SMS = Manifest.permission.READ_SMS
        val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS

        // Location - for context-aware security
        val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        // Microphone - for voice AI
        val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

        // Notifications - for security alerts
        val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

        // Screen capture - for Screen Security Session
        // This is handled differently - it's a special permission
    }

    /**
     * Permission groups for specific features
     */
    object FeaturePermissions {
        val QR_SCANNING = listOf(Permissions.CAMERA)
        val LINK_ANALYSIS = listOf(Permissions.INTERNET, Permissions.NETWORK_STATE)
        val MESSAGE_ANALYSIS = listOf(Permissions.READ_SMS, Permissions.RECEIVE_SMS)
        val CALL_ANALYSIS = listOf(Permissions.READ_CALL_LOG, Permissions.READ_CONTACTS)
        val FILE_ANALYSIS = listOf(
            Permissions.READ_MEDIA_IMAGES,
            Permissions.READ_MEDIA_VIDEO
        )
        val VOICE_AI = listOf(Permissions.RECORD_AUDIO)
        val NOTIFICATIONS = listOf(Permissions.POST_NOTIFICATIONS)
    }

    /**
     * Check if a feature's permissions are granted
     */
    fun isFeaturePermissionGranted(feature: List<String>): Boolean {
        return arePermissionsGranted(feature)
    }

    /**
     * Get missing permissions for a feature
     */
    fun getMissingPermissions(feature: List<String>): List<String> {
        return feature.filter { shouldRequestPermission(it) }
    }

    /**
     * Check if the user has previously denied a permission
     * (for showing explanation dialogs)
     */
    fun shouldShowPermissionRationale(permission: String): Boolean {
        // This would be handled in the UI layer
        // The UI should call ActivityCompat.shouldShowRequestPermissionRationale()
        return false
    }

    /**
     * Check if call log permission is granted
     */
    fun hasReadCallLogPermission(): Boolean {
        return isPermissionGranted(Permissions.READ_CALL_LOG)
    }
}
