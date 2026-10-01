package com.secyra.ai.domain.model

import java.util.Date

data class SecurityEvent(
    val id: String,
    val userId: String,
    val type: SecurityEventType,
    val riskLevel: RiskLevel,
    val title: String,
    val description: String,
    val evidence: List<String> = emptyList(),
    val timestamp: Date = Date()
)

enum class SecurityEventType {
    LINK_ANALYZED,
    QR_ANALYZED,
    MESSAGE_ANALYZED,
    NUMBER_CHECKED,
    SECURITY_WARNING,
    PERMISSION_REVIEW,
    DEVICE_SECURITY_CHECK,
    USER_REPORT
}

enum class RiskLevel {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL,
    UNKNOWN
}
