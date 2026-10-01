package com.secyra.ai.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// SECYRA AI Brand Colors - Exact specification

// Brand Colors - Exact match from specification
val TiffanyBlue = Color(0xFF0ABAB5) // Primary Tiffany
val AzureBlue = Color(0xFF007FFF) // Primary Azure

// Background Colors - Exact match from specification
val SecyraBackground = Color(0xFF000000) // Pure black background
val SecyraSurface = Color(0xFF071018) // Dark blue-black card background
val SecyraSurfaceElevated = Color(0xFF0A151D) // Secondary dark surface
val SecyraDarkBlue = Color(0xFF0A1A2E) // Dark blue for buttons

// Border Colors - Exact match from specification
val SecyraBorder = Color(0xFF172631) // Dark cyan/blue border
val SecyraBorderLight = Color(0xFF253545)

// Text Colors - Exact match from specification
val SecyraWhite = Color(0xFFFFFFFF) // Primary text
val SecyraTextSecondary = Color(0xFFA8C4E8) // Light blue - exact specification
val SecyraTextMuted = Color(0xFF6F8EAD) // Blue-gray - exact specification

// Risk Colors - Exact match from specification
val RiskLow = Color(0xFF4CAF50) // Cyan/green family - safe
val RiskModerate = Color(0xFFFF9800) // Orange/amber - warning
val RiskHigh = Color(0xFFF44336) // Red - error when necessary
val RiskCritical = Color(0xFFD32F2F) // Dark red

// Gradient Brushes - Only using Tiffany to Azure blue family
object BrushColors {
    val verticalGradient = Brush.verticalGradient(
        colors = listOf(
            TiffanyBlue,
            AzureBlue
        )
    )
    val horizontalGradient = Brush.horizontalGradient(
        colors = listOf(
            TiffanyBlue,
            AzureBlue
        )
    )
}
