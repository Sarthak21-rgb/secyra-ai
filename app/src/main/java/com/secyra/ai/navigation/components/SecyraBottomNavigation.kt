package com.secyra.ai.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.navigation.Screen

@Composable
fun SecyraBottomNavigation(
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val screens = listOf(
        Screen.Calls,
        Screen.Scan,
        Screen.Home,
        Screen.AI,
        Screen.Security
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        color = SecyraBackground
    ) {
        Column {
            // Top border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SecyraBorder.copy(alpha = 0.3f))
            )
            
            // Navigation content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(99.dp)
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val selected = currentDestination?.route == screen.route
                    
                    if (screen == Screen.Home) {
                        // Center Home button - raised circular design
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(TiffanyBlue)
                                .clickable { onNavigate(screen) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = stringResource(R.string.nav_home),
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    } else {
                        // Regular navigation items
                        val icon = when (screen) {
                            Screen.Calls -> Icons.Default.Phone
                            Screen.Scan -> Icons.Default.Search
                            Screen.AI -> Icons.Default.Info
                            Screen.Security -> Icons.Default.Lock // Will be replaced with shield icon
                            else -> Icons.Default.Home
                        }
                        val labelRes = when (screen) {
                            Screen.Calls -> R.string.nav_calls
                            Screen.Scan -> R.string.nav_scan
                            Screen.AI -> R.string.nav_ai
                            Screen.Security -> R.string.nav_security
                            else -> R.string.nav_home
                        }

                        Column(
                            modifier = Modifier
                                .width(64.dp)
                                .clickable { onNavigate(screen) }
                                .then(
                                    if (selected) {
                                        Modifier.drawBehind {
                                            drawRoundRect(
                                                color = Color(0xFF1A1A1A),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                                    x = 16.dp.toPx(),
                                                    y = 16.dp.toPx()
                                                )
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (screen == Screen.Security) {
                                // Use Secyra shield logo for Security tab
                                Icon(
                                    painter = painterResource(R.drawable.ic_shield_logo),
                                    contentDescription = stringResource(labelRes),
                                    tint = if (selected) TiffanyBlue else Color(0xFF7FA3C9),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(labelRes),
                                    tint = if (selected) TiffanyBlue else Color(0xFF7FA3C9),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = stringResource(labelRes),
                                color = if (selected) TiffanyBlue else Color(0xFF7FA3C9),
                                fontSize = 15.sp,
                                style = if (selected) {
                                    androidx.compose.ui.text.TextStyle(
                                        shadow = Shadow(
                                            color = TiffanyBlue.copy(alpha = 0.5f),
                                            blurRadius = 8f
                                        )
                                    )
                                } else {
                                    androidx.compose.ui.text.TextStyle.Default
                                }
                            )
                            
                            // Active indicator line for selected tab
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(TiffanyBlue, AzureBlue)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
