package com.secyra.ai.feature.calls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.core.ui.theme.*

/**
 * Activity to show caller information when incoming call notification body is tapped.
 * Shows caller name, DP, location, and spam/trusted status.
 */
class CallerInfoActivity : ComponentActivity() {
    
    private var callerName: String = ""
    private var phoneNumber: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
        
        setContent {
            CallerInfoScreen(
                callerName = callerName,
                phoneNumber = phoneNumber,
                onClose = { finish() }
            )
        }
    }
    
    companion object {
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        
        fun createIntent(callerName: String, phoneNumber: String): Intent {
            return Intent().apply {
                setClassName("com.secyra.ai", "com.secyra.ai.feature.calls.CallerInfoActivity")
                putExtra(EXTRA_CALLER_NAME, callerName)
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            }
        }
    }
}

@Composable
fun CallerInfoScreen(
    callerName: String,
    phoneNumber: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SecyraWhite
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Caller Avatar with custom color
            val avatarColor = getContactColor(callerName, phoneNumber)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (callerName.isNotEmpty()) callerName.first().uppercaseChar().toString() else "#",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Caller Name
            Text(
                text = callerName,
                color = SecyraWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Phone Number
            Text(
                text = phoneNumber,
                color = SecyraTextSecondary,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Location (placeholder - will need real location API)
            LocationCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Spam/Trusted Status
            StatusCard(phoneNumber = phoneNumber)
        }
    }
}

@Composable
fun LocationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Location",
                color = SecyraTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Unknown",
                color = SecyraTextMuted,
                fontSize = 16.sp
            )
            Text(
                text = "Location data requires additional permissions",
                color = SecyraTextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun StatusCard(phoneNumber: String) {
    // For now, default to Unknown - will integrate with real threat detection
    val isSpam = false // TODO: Integrate with threat intelligence
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpam) Color(0xFF1A0A0A) else Color(0xFF0A1A0A)
        ),
        border = BorderStroke(
            1.dp,
            if (isSpam) Color(0xFFFF4D6D) else TiffanyBlue
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSpam) Icons.Default.Warning else Icons.Default.Phone,
                contentDescription = null,
                tint = if (isSpam) Color(0xFFFF4D6D) else TiffanyBlue,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (isSpam) "Spam Caller" else "Trusted",
                color = if (isSpam) Color(0xFFFF4D6D) else TiffanyBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Helper function to get contact color (same as in CallsScreen)
private fun getContactColor(name: String?, phoneNumber: String): Color {
    val CONTACT_COLORS = listOf(
        Color(0xFF007FFF), // Azure Blue
        Color(0xFF0ABAB5), // Tiffany Blue
        Color(0xFF008080), // Teal Blue
        Color(0xFF8A2BE2), // Violet
        Color(0xFFFF9933), // Saffron
        Color(0xFF2E8B57), // Pine
        Color(0xFF00A86B), // Pantone Jade
        Color(0xFF960018), // Carmine
        Color(0xFF8E4585), // Plum
        Color(0xFF003366), // Midnight Blue
        Color(0xFFB8860B), // Dark Goldenrod
        Color(0xFF3CB371), // Medium Sea Green
        Color(0xFFDC143C), // Crimson
        Color(0xFF4B0082), // Indigo
        Color(0xFF228B22), // Forest Green
        Color(0xFF800020), // Maroon
        Color(0xFF191970), // Midnight Blue
        Color(0xFF006400), // Dark Green
        Color(0xFFB22222), // Fire Brick
        Color(0xFF556B2F), // Dark Olive Green
        Color(0xFF00CED1), // Dark Turquoise
        Color(0xFF800000), // Maroon
        Color(0xFF708090)  // Slate Gray
    )
    
    val hash = (name + phoneNumber).hashCode()
    val index = kotlin.math.abs(hash) % CONTACT_COLORS.size
    return CONTACT_COLORS[index]
}
