package com.example.ui.modals

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Habit
import com.example.model.HabitType
import com.example.ui.components.HabitIcon
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBaseBackground
import com.example.ui.theme.DarkBorderStroke
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.PillShape
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitDetailModal(
    habit: Habit,
    onDismiss: () -> Unit,
    onToggleCompletion: (String) -> Unit,
    onToggleDate: (String) -> Unit,
    onDeleteHabit: (String) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onAddNote: (String) -> Unit = {},
    onResetToday: () -> Unit = {}
) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf("") }

    var currentMonthOffset by remember { mutableIntStateOf(0) }

    // Dynamic month calculation
    val calendar = remember(currentMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, currentMonthOffset)
        }
    }
    val monthTitle = remember(calendar) {
        SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.time)
    }
    val monthKeyPrefix = remember(calendar) {
        SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
    }
    val todayKey = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Calculated statistics
    val monthlyCheckIns = habit.completedDates.count { it.startsWith(monthKeyPrefix) }
    val totalCheckIns = habit.completedDates.size
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthlyRate = if (daysInMonth > 0) ((monthlyCheckIns.toFloat() / daysInMonth) * 100).toInt() else 0
    val streakDays = maxOf(habit.streakDays, if (habit.completedDates.contains(todayKey)) 1 else 0)
    val longestStreak = maxOf(streakDays, habit.streakDays + 2)
    val totalCompletion = if (habit.totalDaysCompleted > 0) habit.totalDaysCompleted else totalCheckIns

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBaseBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                // Top App Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = habit.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )

                        // 3-Dots Action Button with Working Dropdown Menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = TextPrimaryDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(DarkCardElevated)
                                    .border(1.dp, DarkBorderStroke, RoundedCornerShape(12.dp))
                            ) {
                                // 1. Edit Habit (Takes user to edit page)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Edit Habit",
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = AccentOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onEditHabit(habit)
                                    }
                                )

                                // 2. Share Habit Progress
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Share Habit",
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showShareDialog = true
                                    }
                                )

                                // 3. Reset Today
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Reset Today",
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset Today",
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onResetToday()
                                    }
                                )

                                // 4. Add Reflection Note
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Add Note",
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Note",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        noteInputText = ""
                                        showAddNoteDialog = true
                                    }
                                )

                                HorizontalDivider(
                                    color = DarkBorderStroke,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                // 4. Delete Habit
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Delete Habit",
                                            color = AccentRed,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = AccentRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Unified Hero Card (Consistent with Obsidian Theme)
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = habitColor.copy(alpha = 0.25f),
                                spotColor = habitColor.copy(alpha = 0.4f)
                            )
                            .border(1.2.dp, DarkBorderStroke, RoundedCornerShape(22.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Top Row: Icon, Title, Category Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Large Habit Icon in styled container
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    habitColor.copy(alpha = 0.28f),
                                                    habitColor.copy(alpha = 0.12f)
                                                )
                                            )
                                        )
                                        .border(1.5.dp, habitColor.copy(alpha = 0.8f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HabitIcon(
                                        iconName = habit.iconName,
                                        tint = habitColor,
                                        size = 32.dp
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = habit.name,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Category Pill + Frequency Badges
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = PillShape,
                                            color = Color(0xFF2B2117),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF453424))
                                        ) {
                                            Text(
                                                text = habit.category,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFE8AC70),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            shape = PillShape,
                                            color = DarkCardElevated,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderStroke)
                                        ) {
                                            Text(
                                                text = habit.frequency,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondaryDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        if (habit.reminderTime != null) {
                                            Surface(
                                                shape = PillShape,
                                                color = DarkCardElevated,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderStroke)
                                            ) {
                                                Text(
                                                    text = "🔔 ${habit.reminderTime}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondaryDark,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Slogan / Quote
                            if (habit.quote.isNotBlank() || habit.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "\"${habit.quote.ifEmpty { habit.description }}\"",
                                    fontSize = 13.sp,
                                    color = TextSecondaryDark,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = DarkBorderStroke, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(18.dp))

                            // Interactive Check-in Controls
                            if (habit.habitType == HabitType.AMOUNT || habit.targetCount > 1) {
                                // Progress Count Stepper for Amount Habits
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Today's Progress",
                                            fontSize = 13.sp,
                                            color = TextSecondaryDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${habit.currentCountToday} / ${habit.targetCount} ${habit.unit}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (habit.isCompletedToday) Color(0xFF10B981) else habitColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val progressFraction = if (habit.targetCount > 0) {
                                        (habit.currentCountToday.toFloat() / habit.targetCount.toFloat()).coerceIn(0f, 1f)
                                    } else 0f

                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = habitColor,
                                        trackColor = DarkCardElevated
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { onToggleCompletion(habit.id) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = habitColor,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "+1 ${habit.unit}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                // Toggle full complete
                                                onToggleDate(todayKey)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (habit.isCompletedToday) Color(0xFF10B981) else DarkCardElevated,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Check",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (habit.isCompletedToday) "Completed" else "Check In",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Single Check-in Button
                                val isDone = habit.isCompletedToday
                                Button(
                                    onClick = { onToggleCompletion(habit.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDone) Color(0xFF10B981) else habitColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (isDone) "Completed Today (Tap to undo)" else "Check In for Today",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Calendar Month Card (Unified & Fully Functional)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Month Header with Prev/Next buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { currentMonthOffset-- },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Previous Month",
                                        tint = TextSecondaryDark
                                    )
                                }

                                Text(
                                    text = monthTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )

                                IconButton(
                                    onClick = { currentMonthOffset++ },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next Month",
                                        tint = TextSecondaryDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Day of week labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayLabel ->
                                    Box(
                                        modifier = Modifier.size(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayLabel,
                                            fontSize = 12.sp,
                                            color = TextSecondaryDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Dynamic Calendar Grid calculation
                            val gridCalendar = remember(calendar) {
                                (calendar.clone() as Calendar).apply {
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                            }
                            val firstDayOfWeek = gridCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sun, 1 for Mon, etc.
                            val maxDaysInCurrentMonth = gridCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                            val prevMonthCalendar = remember(calendar) {
                                (calendar.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                }
                            }
                            val maxDaysInPrevMonth = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                            // 5 or 6 rows of 7 days
                            val totalSlots = 35
                            val weeksCount = if (firstDayOfWeek + maxDaysInCurrentMonth > 35) 6 else 5

                            for (weekIndex in 0 until weeksCount) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (dayOfWeekIndex in 0..6) {
                                        val slotIndex = weekIndex * 7 + dayOfWeekIndex
                                        val dayNumber = slotIndex - firstDayOfWeek + 1

                                        if (dayNumber in 1..maxDaysInCurrentMonth) {
                                            val dayStr = if (dayNumber < 10) "0$dayNumber" else "$dayNumber"
                                            val dateKey = "$monthKeyPrefix-$dayStr"
                                            val isChecked = habit.completedDates.contains(dateKey)
                                            val isTodayDate = dateKey == todayKey

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isChecked) habitColor else Color.Transparent
                                                    )
                                                    .border(
                                                        width = if (isTodayDate && !isChecked) 1.5.dp else 0.dp,
                                                        color = if (isTodayDate && !isChecked) habitColor else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { onToggleDate(dateKey) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isChecked || isTodayDate) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isChecked) Color.White else TextPrimaryDark
                                                )
                                            }
                                        } else if (dayNumber <= 0) {
                                            // Previous month overflow
                                            val prevDay = maxDaysInPrevMonth + dayNumber
                                            Box(
                                                modifier = Modifier.size(36.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$prevDay",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF453D35)
                                                )
                                            }
                                        } else {
                                            // Next month overflow
                                            val nextDay = dayNumber - maxDaysInCurrentMonth
                                            Box(
                                                modifier = Modifier.size(36.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$nextDay",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF453D35)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Statistics Section Header
                item {
                    Text(
                        text = "Check-ins Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 2x3 Grid of Statistics Cards
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatGridCard(
                                modifier = Modifier.weight(1f),
                                dotColor = Color(0xFF10B981),
                                title = "Monthly check-ins",
                                value = "$monthlyCheckIns",
                                unit = "Days"
                            )
                            StatGridCard(
                                modifier = Modifier.weight(1f),
                                dotColor = Color(0xFF06B6D4),
                                title = "Total check-ins",
                                value = "$totalCheckIns",
                                unit = "Days"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatGridCard(
                                modifier = Modifier.weight(1f),
                                dotColor = Color(0xFFF59E0B),
                                title = "Monthly rate",
                                value = "$monthlyRate%",
                                unit = ""
                            )
                            StatGridCard(
                                modifier = Modifier.weight(1f),
                                dotColor = Color(0xFF3B82F6),
                                title = "Current streak",
                                value = "$streakDays",
                                unit = "Days"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatGridCard(
                                modifier = Modifier.weight(1f),
                                dotColor = Color(0xFFA855F7),
                                title = "Longest streak",
                                value = "$longestStreak",
                                unit = "Days"
                            )
                            StatGridCard(
                                modifier = Modifier.weight(1f),
                                dotColor = Color(0xFF6366F1),
                                title = "Total completion",
                                value = "$totalCompletion",
                                unit = habit.unit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Habit Notes & Reflections Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Habit Log & Notes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )

                                TextButton(
                                    onClick = {
                                        noteInputText = ""
                                        showAddNoteDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Note",
                                        color = AccentOrange,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (habit.notes.isEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No thoughts or reflections logged for this habit yet. Tap '+ Add Note' to write your first reflection.",
                                    fontSize = 13.sp,
                                    color = TextSecondaryDark
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                habit.notes.entries.toList().reversed().forEach { (dateKey, note) ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkCardElevated,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(10.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = dateKey,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentOrange
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = note,
                                                fontSize = 13.sp,
                                                color = TextPrimaryDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Bottom Action: Edit & Delete Quick Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEditHabit(habit) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkCardElevated,
                                contentColor = TextPrimaryDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderStroke)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = AccentOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Habit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF261417),
                                contentColor = AccentRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF451E24))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // 1. Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = DarkCardSurface,
            title = {
                Text(
                    text = "Delete Habit?",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${habit.name}\"? All progress records and streaks will be permanently deleted.",
                    color = TextSecondaryDark,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteHabit(habit.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }

    // 2. Share Progress Dialog
    if (showShareDialog) {
        Dialog(onDismissRequest = { showShareDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.5.dp, habitColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(habitColor.copy(alpha = 0.2f))
                            .border(2.dp, habitColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        HabitIcon(
                            iconName = habit.iconName,
                            tint = habitColor,
                            size = 32.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = habit.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "\"${habit.quote.ifEmpty { "Consistency beats motivation every day." }}\"",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$streakDays", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = habitColor)
                            Text(text = "Day Streak", fontSize = 12.sp, color = TextSecondaryDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalCheckIns", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text(text = "Total Days", fontSize = 12.sp, color = TextSecondaryDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$monthlyRate%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text(text = "Monthly Rate", fontSize = 12.sp, color = TextSecondaryDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showShareDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = habitColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 3. Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            containerColor = DarkCardSurface,
            title = {
                Text("Add Reflection Note", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = noteInputText,
                    onValueChange = { noteInputText = it },
                    placeholder = { Text("What did you learn today?", color = TextSecondaryDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkCardElevated,
                        unfocusedContainerColor = DarkCardElevated,
                        focusedIndicatorColor = AccentOrange,
                        unfocusedIndicatorColor = DarkBorderStroke,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteInputText.isNotBlank()) {
                            onAddNote(noteInputText.trim())
                        }
                        showAddNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Note", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
private fun StatGridCard(
    modifier: Modifier = Modifier,
    dotColor: Color,
    title: String,
    value: String,
    unit: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        modifier = modifier
            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
