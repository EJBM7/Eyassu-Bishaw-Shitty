package com.example.ui.screens.settings

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppSettings
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CardShape
import com.example.ui.theme.InnerCardShape
import com.example.ui.theme.PillShape

@Composable
fun SettingsScreen(
    settings: AppSettings,
    habitsCount: Int,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateAppearance: () -> Unit,
    onNavigatePreferences: () -> Unit,
    onNavigateBackups: () -> Unit,
    onNavigateSecurity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(settings.accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(settings.accentColorHex))
        } catch (_: Exception) {
            Color(0xFFEE6716)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Title
        item {
            Text(
                text = "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
            )
        }

        // Profile Card
        item {
            ProfileHeaderCard(
                userName = settings.userName,
                userPlan = settings.userPlan,
                habitsCount = habitsCount,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Theme & Appearance Card
        item {
            ThemeAndAppearanceCard(
                settings = settings,
                onUpdateSettings = onUpdateSettings,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Preferences & Setup Section Card
        item {
            PreferencesSetupCard(
                settings = settings,
                accentColor = accentColor,
                onNavigateAppearance = onNavigateAppearance,
                onNavigatePreferences = onNavigatePreferences,
                onNavigateBackups = onNavigateBackups,
                onNavigateSecurity = onNavigateSecurity
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Footer Craftsmanship Notice
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Habit Tracker v1.2 · Designed with Apple iOS Craftsmanship",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    userName: String,
    userPlan: String,
    habitsCount: Int,
    accentColor: Color
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF24242A), CardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile image / avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF26262F))
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(42.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit name",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = PillShape,
                        color = accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = userPlan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "• $habitsCount habits active",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeAndAppearanceCard(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    accentColor: Color
) {
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
                text = "Theme & Appearance",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Light vs Dark Mode Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Light mode preview card
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Light",
                    isSelected = !settings.isDarkMode,
                    accentColor = accentColor,
                    previewBg = Color(0xFFF3F4F6),
                    previewCard = Color.White,
                    onClick = { onUpdateSettings { it.copy(isDarkMode = false) } }
                )

                // Dark mode preview card
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Dark",
                    isSelected = settings.isDarkMode,
                    accentColor = accentColor,
                    previewBg = Color(0xFF0B0B0B),
                    previewCard = Color(0xFF1E1E22),
                    onClick = { onUpdateSettings { it.copy(isDarkMode = true) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match System Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match System (Automatic)",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = settings.matchSystem,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(matchSystem = checked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor,
                        uncheckedTrackColor = Color(0xFF26262F)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sound Effects Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sound Effects",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = settings.soundEffects,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(soundEffects = checked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor,
                        uncheckedTrackColor = Color(0xFF26262F)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Completed Task Style Segmented Control
            Text(
                text = "Completed Task Style",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = PillShape,
                color = Color(0xFF1E1E24),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    val isFade = settings.completedTaskStyle == "Fade"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isFade) accentColor else Color.Transparent)
                            .clickable { onUpdateSettings { it.copy(completedTaskStyle = "Fade") } }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fade",
                            fontSize = 13.sp,
                            fontWeight = if (isFade) FontWeight.Bold else FontWeight.Normal,
                            color = if (isFade) Color.White else Color(0xFF9CA3AF)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (!isFade) accentColor else Color.Transparent)
                            .clickable { onUpdateSettings { it.copy(completedTaskStyle = "Crossline") } }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Crossline",
                            fontSize = 13.sp,
                            fontWeight = if (!isFade) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isFade) Color.White else Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    previewBg: Color,
    previewCard: Color,
    onClick: () -> Unit
) {
    Card(
        shape = InnerCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1F)),
        modifier = modifier
            .clip(InnerCardShape)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFF2A2A32),
                shape = InnerCardShape
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Miniature UI preview mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(previewBg)
                    .border(0.5.dp, Color(0xFF33333C), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(previewCard)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
private fun PreferencesSetupCard(
    settings: AppSettings,
    accentColor: Color,
    onNavigateAppearance: () -> Unit,
    onNavigatePreferences: () -> Unit,
    onNavigateBackups: () -> Unit,
    onNavigateSecurity: () -> Unit
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF24242A), CardShape)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PREFERENCES & SETUP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavRow(
                icon = Icons.Filled.ColorLens,
                iconColor = accentColor,
                title = "Appearance",
                subtitle = if (settings.isDarkMode) "Dark" else "Light",
                onClick = onNavigateAppearance
            )

            SettingsNavRow(
                icon = Icons.Filled.Tune,
                iconColor = AccentAmber,
                title = "Preferences",
                subtitle = if (settings.weekStartsOnMonday) "Monday" else "Sunday",
                onClick = onNavigatePreferences
            )

            SettingsNavRow(
                icon = Icons.Filled.Lock,
                iconColor = accentColor,
                title = "Security & App Lock",
                subtitle = if (settings.appLockEnabled) "Phone Lock Enabled" else "Off",
                onClick = onNavigateSecurity
            )

            SettingsNavRow(
                icon = Icons.Filled.Storage,
                iconColor = Color(0xFF10B981),
                title = "Data & Backups",
                subtitle = null,
                onClick = onNavigateBackups
            )
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (badge != null) {
                Surface(
                    shape = PillShape,
                    color = AccentGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
