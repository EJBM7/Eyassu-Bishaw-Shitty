package com.example.ui.screens.settings

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.ui.theme.CardShape
import com.example.ui.theme.PillShape

@Composable
fun SecurityScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = remember(settings.accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(settings.accentColorHex))
        } catch (_: Exception) {
            Color(0xFFEE6716)
        }
    }

    val keyguardManager = remember {
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
    val isDeviceSecure = remember { keyguardManager?.isKeyguardSecure == true }

    var testStatusMessage by remember { mutableStateOf<String?>(null) }

    val testLockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            testStatusMessage = "Authentication successful!"
            Toast.makeText(context, "Phone lock verified successfully!", Toast.LENGTH_SHORT).show()
        } else {
            testStatusMessage = "Authentication cancelled or failed."
        }
    }

    val toggleLockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onUpdateSettings { it.copy(appLockEnabled = true, appLockType = "phone_lock") }
            Toast.makeText(context, "App lock enabled with phone lock!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Lock confirmation cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Security & App Lock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // App Lock Toggle Card
        item {
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, Color(0xFF24242A), CardShape)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Enable App Lock",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Require phone lock to open",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        Switch(
                            checked = settings.appLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (isDeviceSecure && keyguardManager != null) {
                                        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                                            "Enable App Lock",
                                            "Confirm your device lock to enable App Lock for Habit Tracker"
                                        )
                                        if (intent != null) {
                                            toggleLockLauncher.launch(intent)
                                        } else {
                                            onUpdateSettings { it.copy(appLockEnabled = true, appLockType = "phone_lock") }
                                        }
                                    } else {
                                        onUpdateSettings { it.copy(appLockEnabled = true, appLockType = "phone_lock") }
                                        Toast.makeText(context, "App lock enabled (Note: No device PIN set)", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    onUpdateSettings { it.copy(appLockEnabled = false) }
                                    Toast.makeText(context, "App lock disabled", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedTrackColor = Color(0xFF26262F)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Device Security Status
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDeviceSecure) Color(0xFF14241B) else Color(0xFF2B1F14),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDeviceSecure) Color(0xFF1B4D2E) else Color(0xFF5C381E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isDeviceSecure) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isDeviceSecure) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isDeviceSecure) "Device Lock Active" else "No Screen Lock Detected",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeviceSecure) Color(0xFF34D399) else Color(0xFFFBBF24)
                                )
                                Text(
                                    text = if (isDeviceSecure) {
                                        "Uses system fingerprint, face unlock, pattern, or device PIN."
                                    } else {
                                        "Set up a screen lock in phone settings for maximum security."
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    if (settings.appLockEnabled && isDeviceSecure) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Test Phone Lock Button
                        Surface(
                            shape = PillShape,
                            color = Color(0xFF221E1A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382E25)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PillShape)
                                .clickable {
                                    if (keyguardManager != null) {
                                        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                                            "Test Phone Lock",
                                            "Verify your phone lock credentials"
                                        )
                                        if (intent != null) {
                                            testLockLauncher.launch(intent)
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Test Phone Lock Prompt",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        if (testStatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = testStatusMessage ?: "",
                                fontSize = 12.sp,
                                color = accentColor,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Privacy & Protection Info Card
        item {
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, Color(0xFF24242A), CardShape)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HOW PHONE LOCK WORKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Uses the native Android Keyguard & Biometrics API directly.\n" +
                                "• The app never stores or accesses your PIN, password, or biometric data.\n" +
                                "• When enabled, the lock screen appears immediately upon launching the app or returning to it.\n" +
                                "• Supports Fingerprint, Face recognition, Pattern, and alphanumeric Passcode.",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
