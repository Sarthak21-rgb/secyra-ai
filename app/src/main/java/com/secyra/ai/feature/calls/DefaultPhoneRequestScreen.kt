package com.secyra.ai.feature.calls

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.R
import com.secyra.ai.core.telecom.DefaultPhoneRoleManager
import com.secyra.ai.core.ui.theme.*

@Composable
fun DefaultPhoneRequestScreen(
    onRequestDefaultPhone: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SecyraSurface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, TiffanyBlue),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Shield icon
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_shield_logo),
                    contentDescription = "SECYRA AI",
                    modifier = Modifier.size(80.dp)
                )

                // Title
                Text(
                    text = "Enable Secyra Calling",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = "To show Secyra's secure calling interface for your phone calls, Android requires Secyra to be your default phone app.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecyraTextSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Important note
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TiffanyBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = TiffanyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "You can change this back anytime in Android settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TiffanyBlue,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Button(
                    onClick = onRequestDefaultPhone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TiffanyBlue,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Set as Default Phone App",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SecyraTextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        SecyraBorder
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Use Basic Calling",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
