package com.secyra.ai.core.telecom

import android.content.ComponentName
import android.content.Context
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telecom.TelecomManager.PRESENTATION_ALLOWED
import android.graphics.drawable.Icon
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers Secyra's PhoneAccount with Android Telecom.
 * 
 * This is required for Secyra to appear as a candidate for default phone app.
 * Without this, Android won't show Secyra in the default app selection.
 */
@Singleton
class PhoneAccountRegistrar @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

    private val componentName = ComponentName(context, SecyraInCallService::class.java)
    private val accountHandle = PhoneAccountHandle(componentName, "secyra_account_1")

    /**
     * Register Secyra's PhoneAccount with Android Telecom
     */
    fun registerPhoneAccount() {
        val telecom = telecomManager ?: return

        try {
            // Check if account already exists
            val existingAccount = telecom.getPhoneAccount(accountHandle)
            if (existingAccount != null) {
                // Account already registered
                return
            }

            // Create PhoneAccount
            val phoneAccount = PhoneAccount.Builder(
                accountHandle,
                "Secyra AI"
            ).apply {
                // Set account capabilities
                setCapabilities(
                    PhoneAccount.CAPABILITY_CALL_PROVIDER or
                    PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION or
                    PhoneAccount.CAPABILITY_VIDEO_CALLING or
                    PhoneAccount.CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE
                )

                // Set icon (using app icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setIcon(Icon.createWithResource(context, android.R.drawable.sym_def_app_icon))
                }

                // Set short description
                setShortDescription("Secyra AI Secure Calling")

                // Set supported URI schemes
                setSupportedUriSchemes(listOf("tel"))
            }.build()

            // Register the account
            telecom.registerPhoneAccount(phoneAccount)

            // Note: On newer Android versions, accounts are auto-enabled
            // The enablePhoneAccount method was deprecated

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Unregister Secyra's PhoneAccount
     */
    fun unregisterPhoneAccount() {
        val telecom = telecomManager ?: return

        try {
            telecom.unregisterPhoneAccount(accountHandle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get the PhoneAccountHandle
     */
    fun getPhoneAccountHandle(): PhoneAccountHandle {
        return accountHandle
    }

    /**
     * Check if the account is registered
     */
    fun isAccountRegistered(): Boolean {
        val telecom = telecomManager ?: return false
        return telecom.getPhoneAccount(accountHandle) != null
    }
}
