package com.example.ui.screens.settings

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CardShape
import com.example.ui.theme.PillShape

@Composable
fun PreferencesScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit,
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
        // Back Header
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
                    text = "Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // GENERAL section
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
                        text = "GENERAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Week starts: Mon / Sun
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Week starts",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = PillShape,
                            color = Color(0xFF1E1E24)
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                val isMon = settings.weekStartsOnMonday
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(if (isMon) accentColor else Color.Transparent)
                                        .clickable { onUpdateSettings { it.copy(weekStartsOnMonday = true) } }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Mon",
                                        fontSize = 12.sp,
                                        fontWeight = if (isMon) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isMon) Color.White else Color(0xFF9CA3AF)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(if (!isMon) accentColor else Color.Transparent)
                                        .clickable { onUpdateSettings { it.copy(weekStartsOnMonday = false) } }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Sun",
                                        fontSize = 12.sp,
                                        fontWeight = if (!isMon) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!isMon) Color.White else Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Effects
                    PreferenceToggleRow(
                        title = "Sound Effects",
                        checked = settings.soundEffects,
                        accentColor = accentColor,
                        onCheckedChange = { onUpdateSettings { it.copy(soundEffects = it.soundEffects.not()) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptic Feedback
                    PreferenceToggleRow(
                        title = "Haptic Feedback",
                        checked = settings.hapticFeedback,
                        accentColor = accentColor,
                        onCheckedChange = { onUpdateSettings { it.copy(hapticFeedback = it.hapticFeedback.not()) } }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // TASKS section
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
                        text = "TASKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completed Task Style",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = PillShape,
                            color = Color(0xFF1E1E24)
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                val isFade = settings.completedTaskStyle == "Fade"
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(if (isFade) accentColor else Color.Transparent)
                                        .clickable { onUpdateSettings { it.copy(completedTaskStyle = "Fade") } }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Fade",
                                        fontSize = 12.sp,
                                        fontWeight = if (isFade) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isFade) Color.White else Color(0xFF9CA3AF)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(if (!isFade) accentColor else Color.Transparent)
                                        .clickable { onUpdateSettings { it.copy(completedTaskStyle = "Crossline") } }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Crossline",
                                        fontSize = 12.sp,
                                        fontWeight = if (!isFade) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!isFade) Color.White else Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // TODAY VIEW section
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
                        text = "TODAY VIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PreferenceToggleRow(
                        title = "Activity on cards",
                        checked = settings.showActivityOnCards,
                        accentColor = accentColor,
                        onCheckedChange = { onUpdateSettings { it.copy(showActivityOnCards = it.showActivityOnCards.not()) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PreferenceToggleRow(
                        title = "Quotes",
                        checked = settings.showQuotes,
                        accentColor = accentColor,
                        onCheckedChange = { onUpdateSettings { it.copy(showQuotes = it.showQuotes.not()) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PreferenceToggleRow(
                        title = "Connect the days",
                        checked = settings.connectDays,
                        accentColor = accentColor,
                        onCheckedChange = { onUpdateSettings { it.copy(connectDays = it.connectDays.not()) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = Color(0xFF26262F)
            )
        )
    }
}
