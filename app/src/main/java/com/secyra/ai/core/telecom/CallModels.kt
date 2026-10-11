package com.secyra.ai.core.telecom

/**
 * Caller identification information.
 */
data class CallerInfo(
    val phoneNumber: String,
    val name: String?,
    val photoUri: String?,
    val isContact: Boolean,
    val isVerified: Boolean = false,
    val verificationSource: String? = null
)