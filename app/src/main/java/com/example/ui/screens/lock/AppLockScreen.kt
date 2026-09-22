package com.example.ui.screens.lock

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentOrange

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit,
    accentColor: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyguardManager = remember {
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
    val isDeviceSecure = remember { keyguardManager?.isKeyguardSecure == true }

    var authError by remember { mutableStateOf<String?>(null) }
    var authAttempted by remember { mutableStateOf(false) }

    val unlockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authError = null
            onUnlocked()
        } else {
            authError = "Authentication was cancelled or failed. Please try again."
        }
    }

    fun promptDeviceUnlock() {
        authAttempted = true
        if (keyguardManager != null && isDeviceSecure) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "Unlock Habit Tracker",
                "Confirm your device lock (Fingerprint, PIN, Pattern, or Password) to access your habits"
            )
            if (intent != null) {
                unlockLauncher.launch(intent)
            } else {
                // If intent couldn't be created, allow unlock
                onUnlocked()
            }
        } else {
            // Device has no screen lock configured
            onUnlocked()
        }
    }

    // Automatically prompt when screen first loads
    LaunchedEffect(Unit) {
        if (isDeviceSecure) {
            promptDeviceUnlock()
        } else {
            onUnlocked()
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0D0B))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing Lock Icon Container
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                Color(0xFF1D1712)
                            )
                        )
                    )
                    .border(2.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "App Locked",
                    tint = accentColor,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Habit Tracker Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isDeviceSecure) {
                    "Protected by your phone's native lock.\nUse fingerprint, face, PIN, or pattern to unlock."
                } else {
                    "App lock is enabled, but no screen lock is set up on this device."
                },
                fontSize = 14.sp,
                color = Color(0xFF9E968F),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (authError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF331414),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5C2020))
                ) {
                    Text(
                        text = authError ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFF87171),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Unlock Button using Phone Native Lock
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { promptDeviceUnlock() }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isDeviceSecure) Icons.Filled.Fingerprint else Icons.Filled.LockOpen,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = if (isDeviceSecure) "Unlock with Phone Lock" else "Continue to App",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isDeviceSecure) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tip: Set a screen lock (PIN, Pattern, or Fingerprint) in Android Settings for full security.",
                    fontSize = 12.sp,
                    color = Color(0xFF756F68),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
