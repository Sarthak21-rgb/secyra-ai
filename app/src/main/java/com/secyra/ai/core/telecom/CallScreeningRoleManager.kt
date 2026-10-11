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
 * Manages the Android ROLE_CALL_SCREENING for caller ID and call screening.
 *
 * This role allows Secyra to:
 * - Identify incoming callers
 * - Screen calls before they ring
 * - Display caller information
 * - Block spam calls (with user permission)
 *
 * IMPORTANT:
 * - The user must explicitly grant this role through Android's system UI
 * - We cannot silently assign this role
 * - The role can be revoked by the user at any time
 * - Some devices may not support this role
 *
 * @see RoleManager.ROLE_CALL_SCREENING
 */
@Singleton
class CallScreeningRoleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val roleManager: RoleManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
    } else {
        null
    }

    private val _isRoleAvailable = MutableStateFlow(false)
    val isRoleAvailable: StateFlow<Boolean> = _isRoleAvailable.asStateFlow()

    private val _isRoleHeld = MutableStateFlow(false)
    val isRoleHeld: StateFlow<Boolean> = _isRoleHeld.asStateFlow()

    private val _isRoleSupported = MutableStateFlow(false)
    val isRoleSupported: StateFlow<Boolean> = _isRoleSupported.asStateFlow()

    init {
        checkRoleStatus()
    }

    /**
     * Check if the call screening role is available, supported, and currently held.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkRoleStatus() {
        val rm = roleManager ?: run {
            _isRoleAvailable.value = false
            _isRoleSupported.value = false
            _isRoleHeld.value = false
            return
        }

        // Check if the role is available on this device
        _isRoleAvailable.value = rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)

        // Check if the role is supported by the device
        _isRoleSupported.value = rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)

        // Check if Secyra currently holds the role
        _isRoleHeld.value = rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    /**
     * Create an intent to request the call screening role.
     *
     * This will open Android's official role selection UI where the user can:
     * - Grant the role to Secyra
     * - Deny the role
     * - Choose a different app
     *
     * Returns null if the role is not available on this device.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createRoleRequestIntent(): Intent? {
        val rm = roleManager ?: return null

        if (!rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            return null
        }

        return rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    }

    /**
     * Check if Secyra is the current call screening app.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isCallScreeningApp(): Boolean {
        val rm = roleManager ?: return false
        return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    /**
     * Get a user-friendly status message about the call screening role.
     */
    fun getStatusMessage(): String {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                "Call screening requires Android 10 or higher"
            }
            !isRoleAvailable.value -> {
                "Call screening is not available on this device"
            }
            !isRoleHeld.value -> {
                "Secyra is not set as the call screening app"
            }
            else -> {
                "Secyra is active as the call screening app"
            }
        }
    }

    /**
     * Refresh the role status (call this after returning from role request UI).
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun refreshStatus() {
        checkRoleStatus()
    }
}