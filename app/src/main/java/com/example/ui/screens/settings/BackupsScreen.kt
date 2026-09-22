package com.example.ui.screens.settings

import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitRepository
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CardShape
import com.example.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupsScreen(
    onResetSampleData: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { HabitRepository.instance }
    val archivedHabits by repository.archivedHabits.collectAsState()

    var showClearProgressDialog by remember { mutableStateOf(false) }
    var showEraseAllDialog by remember { mutableStateOf(false) }
    var showOtherAppsDialog by remember { mutableStateOf(false) }
    var showAutoBackupDialog by remember { mutableStateOf(false) }
    var showArchivedHabitsDialog by remember { mutableStateOf(false) }

    var autoBackupFrequency by remember { mutableStateOf("Off") }
    var selectedExternalAppType by remember { mutableStateOf("TickTick") }

    // System File Picker for Exporting JSON
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = repository.exportBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to export backup: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // System File Picker for Importing Backup JSON
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: ""
                val success = repository.importBackupJson(content)
                if (success) {
                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Invalid backup format.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to import: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // System File Picker for Importing from Other Apps (TickTick, Loop, HabitKit)
    val importOtherAppLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: ""
                val count = repository.importFromExternalApp(selectedExternalAppType, content)
                if (count > 0) {
                    Toast.makeText(context, "Imported $count habits from $selectedExternalAppType!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "No habits could be parsed from the file.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to parse: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                    .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
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
                    text = "Data",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .border(1.dp, Color(0xFF24242A), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                    Text(
                        text = "BACKUPS AND IMPORT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8A827B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Import from another app [BETA]
                    DataSettingRow(
                        icon = Icons.Filled.SyncAlt,
                        title = "Import from another app",
                        subtitle = "Loop Habit Tracker, HabitKit, TickTick...",
                        badgeText = "BETA",
                        badgeColor = AccentPink,
                        onClick = { showOtherAppsDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFF222228), modifier = Modifier.padding(vertical = 4.dp))

                    // 2. Import backup
                    DataSettingRow(
                        icon = Icons.Filled.FolderOpen,
                        title = "Import backup",
                        subtitle = "Restore habits from a file",
                        onClick = {
                            importBackupLauncher.launch("*/*")
                        }
                    )

                    HorizontalDivider(color = Color(0xFF222228), modifier = Modifier.padding(vertical = 4.dp))

                    // 3. Export backup
                    DataSettingRow(
                        icon = Icons.Filled.FileUpload,
                        title = "Export backup",
                        subtitle = "Save all habits to a file",
                        onClick = {
                            val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            exportLauncher.launch("habit_tracker_backup_$timeStr.json")
                        }
                    )

                    HorizontalDivider(color = Color(0xFF222228), modifier = Modifier.padding(vertical = 4.dp))

                    // 4. Automatic backup
                    DataSettingRow(
                        icon = Icons.Filled.CloudUpload,
                        title = "Automatic backup",
                        subtitle = autoBackupFrequency,
                        onClick = { showAutoBackupDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFF222228), modifier = Modifier.padding(vertical = 4.dp))

                    // 5. Archived habits
                    DataSettingRow(
                        icon = Icons.Filled.Archive,
                        title = "Archived habits",
                        subtitle = if (archivedHabits.isEmpty()) "Restore them or delete them for good" else "${archivedHabits.size} archived habits",
                        onClick = { showArchivedHabitsDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFF222228), modifier = Modifier.padding(vertical = 4.dp))

                    // 6. Clear progress
                    DataSettingRow(
                        icon = Icons.Filled.LayersClear,
                        title = "Clear progress",
                        subtitle = "History, notes and focus sessions",
                        onClick = { showClearProgressDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFF222228), modifier = Modifier.padding(vertical = 4.dp))

                    // 7. Erase all data
                    DataSettingRow(
                        icon = Icons.Filled.Warning,
                        title = "Erase all data",
                        subtitle = "Habits, notes, focus sessions and settings",
                        iconTint = AccentRed,
                        titleColor = AccentRed,
                        onClick = { showEraseAllDialog = true }
                    )
                }
            }
        }
    }

    // Dialog: Import from another app
    if (showOtherAppsDialog) {
        AlertDialog(
            onDismissRequest = { showOtherAppsDialog = false },
            title = { Text("Import from another app", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select which app export format you want to import into Habit Tracker:",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    listOf(
                        "TickTick (CSV / Backup)",
                        "Loop Habit Tracker (CSV)",
                        "HabitKit (JSON)"
                    ).forEach { appName ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF202026),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExternalAppType = appName
                                    showOtherAppsDialog = false
                                    importOtherAppLauncher.launch("*/*")
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = appName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF6B7280))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOtherAppsDialog = false }) {
                    Text("Cancel", color = Color(0xFF9CA3AF))
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }

    // Dialog: Auto backup frequency
    if (showAutoBackupDialog) {
        AlertDialog(
            onDismissRequest = { showAutoBackupDialog = false },
            title = { Text("Automatic Backup", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Off", "Daily", "Weekly", "Monthly").forEach { freq ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (autoBackupFrequency == freq) AccentOrange.copy(alpha = 0.2f) else Color(0xFF222228),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    autoBackupFrequency = freq
                                    showAutoBackupDialog = false
                                    Toast.makeText(context, "Automatic backup set to $freq", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = freq,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (autoBackupFrequency == freq) AccentOrange else Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoBackupDialog = false }) {
                    Text("Close", color = Color(0xFF9CA3AF))
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }

    // Dialog: Archived habits
    if (showArchivedHabitsDialog) {
        AlertDialog(
            onDismissRequest = { showArchivedHabitsDialog = false },
            title = { Text("Archived Habits", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                if (archivedHabits.isEmpty()) {
                    Text("No habits have been archived yet.", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        archivedHabits.forEach { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF222228))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = {
                                            repository.restoreArchivedHabit(habit.id)
                                            Toast.makeText(context, "Restored ${habit.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = AccentOrange, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            repository.permanentlyDeleteArchivedHabit(habit.id)
                                            Toast.makeText(context, "Permanently deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.DeleteForever, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchivedHabitsDialog = false }) {
                    Text("Done", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }

    // Dialog: Clear progress
    if (showClearProgressDialog) {
        AlertDialog(
            onDismissRequest = { showClearProgressDialog = false },
            title = { Text("Clear Progress?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "This will clear all completion records, streaks, and focus history, while leaving your habits and settings intact.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.clearAllProgress()
                        showClearProgressDialog = false
                        Toast.makeText(context, "All progress has been reset.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear Progress", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearProgressDialog = false }) {
                    Text("Cancel", color = Color(0xFF9CA3AF))
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }

    // Dialog: Erase all data
    if (showEraseAllDialog) {
        AlertDialog(
            onDismissRequest = { showEraseAllDialog = false },
            title = { Text("Erase All Data?", fontWeight = FontWeight.Bold, color = AccentRed) },
            text = {
                Text(
                    "This is irreversible! All your habits, tasks, history, notes, and custom preferences will be completely deleted.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.eraseAllData()
                        showEraseAllDialog = false
                        Toast.makeText(context, "All data has been erased.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Erase Everything", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEraseAllDialog = false }) {
                    Text("Cancel", color = Color(0xFF9CA3AF))
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }
}

@Composable
private fun DataSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = AccentPink,
    iconTint: Color = Color(0xFF8A827B),
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )

                    if (badgeText != null) {
                        Surface(
                            shape = PillShape,
                            color = badgeColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF8A827B)
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Open",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(18.dp)
        )
    }
}
