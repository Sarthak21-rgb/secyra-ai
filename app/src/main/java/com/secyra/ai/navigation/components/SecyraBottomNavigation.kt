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
import androidx.compose.ui.graphics.Color
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
            .height(82.dp),
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
                    .height(81.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val selected = currentDestination?.route == screen.route

                    val icon = when (screen) {
                        Screen.Calls -> Icons.Default.Phone
                        Screen.Scan -> Icons.Default.Search
                        Screen.Home -> Icons.Default.Home
                        Screen.AI -> Icons.Default.Info
                        Screen.Security -> Icons.Default.Lock // Will be replaced with shield icon
                        else -> Icons.Default.Home
                    }
                    val labelRes = when (screen) {
                        Screen.Calls -> R.string.nav_calls
                        Screen.Scan -> R.string.nav_scan
                        Screen.Home -> R.string.nav_home
                        Screen.AI -> R.string.nav_ai
                        Screen.Security -> R.string.nav_security
                        else -> R.string.nav_home
                    }

                    Column(
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onNavigate(screen) },
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
                            fontSize = 13.sp
                        )

                        // Active indicator line for selected tab
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(3.dp)
                                    .background(TiffanyBlue)
                            )
                        }
                    }
                }
            }
        }
    }
}
