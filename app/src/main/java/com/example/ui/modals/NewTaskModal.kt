package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Task
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.PriorityHighBg
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowBg
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumBg
import com.example.ui.theme.PriorityMediumColor
import com.example.ui.theme.PriorityNoneBg
import com.example.ui.theme.PriorityNoneColor
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskModal(
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit,
    onUpdateTask: ((Task) -> Unit)? = null,
    onDeleteTask: ((String) -> Unit)? = null,
    taskToEdit: Task? = null,
    onOpenDatePicker: (() -> Unit)? = null,
    selectedDateTimeLabel: String = "Today · 11:00 PM"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEditing = taskToEdit != null
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedDate by remember { mutableStateOf(taskToEdit?.dueDate ?: "Today") }
    var selectedTime by remember { mutableStateOf(taskToEdit?.dueTime ?: "None") }
    var selectedPriority by remember { mutableIntStateOf(taskToEdit?.priority ?: 1) } // 0: None, 1: Low, 2: Medium, 3: High
    var category by remember { mutableStateOf(taskToEdit?.category ?: "Personal") }
    var selectedReminder by remember { mutableStateOf(taskToEdit?.reminder ?: "None") }
    var selectedRepeat by remember { mutableStateOf(taskToEdit?.repeat ?: "None") }
    var hasPhotoAttached by remember { mutableStateOf(false) }

    // Dropdowns / Modals
    var showCalendarModal by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val priorityLabel = when (selectedPriority) {
        3 -> "High"
        2 -> "Medium"
        1 -> "Low"
        else -> null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFF15120F), // Warm obsidian container matching screenshot
        tonalElevation = 6.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            // Optional Top bar for Edit Mode with Delete Button
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDIT TASK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E968F),
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onDeleteTask != null && taskToEdit != null) {
                            IconButton(
                                onClick = {
                                    onDeleteTask(taskToEdit.id)
                                    onDismiss()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Task",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF9E968F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Top Chip Badges Row (Date, Time, Priority, Category)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Chip [ 📅 Sep 20 / Today  ✕ ]
                if (selectedDate.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF261F18),
                        modifier = Modifier.clickable { showCalendarModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Date",
                                tint = AccentOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = selectedDate,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentOrange
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear date",
                                tint = AccentOrange,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { selectedDate = "" }
                            )
                        }
                    }
                }

                // Time Chip
                if (selectedTime.isNotBlank() && selectedTime != "None") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF261F18),
                        modifier = Modifier.clickable { showCalendarModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = "Time",
                                tint = AccentOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = selectedTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentOrange
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear time",
                                tint = AccentOrange,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { selectedTime = "None" }
                            )
                        }
                    }
                }

                // Priority Chip [ 🚩 High / Medium / Low ✕ ]
                if (priorityLabel != null) {
                    val chipBg = when (selectedPriority) {
                        3 -> PriorityHighBg
                        2 -> PriorityMediumBg
                        else -> PriorityLowBg
                    }
                    val chipTint = when (selectedPriority) {
                        3 -> PriorityHighColor
                        2 -> PriorityMediumColor
                        else -> PriorityLowColor
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = chipBg,
                        modifier = Modifier.clickable { showPriorityMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = "Priority",
                                tint = chipTint,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = priorityLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = chipTint
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear priority",
                                tint = chipTint,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { selectedPriority = 0 }
                            )
                        }
                    }
                }

                // Category Chip [ 🏷️ Personal ✕ ]
                if (category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF382F24),
                        modifier = Modifier.clickable { showCategoryMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Label,
                                contentDescription = "Category",
                                tint = Color(0xFFE8AC70),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE8AC70)
                            )
                        }
                    }
                }
            }

            // Task Title TextField
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "Add a task...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF756F68)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AccentOrange,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Description / Notes Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        text = "# Description",
                        fontSize = 14.sp,
                        color = Color(0xFF756F68)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFD6D1CA)
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AccentOrange,
                    focusedTextColor = Color(0xFFD6D1CA),
                    unfocusedTextColor = Color(0xFFD6D1CA)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Action Row matching Screenshot_20260920-045341_TickTick.jpg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Actions (Calendar, Flag, Tag, Image, Camera)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 📅 Calendar Icon (Tapping opens the full Calendar sheet!)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedDate.isNotBlank()) Color(0xFF2B221A) else Color.Transparent,
                        modifier = Modifier.clickable { showCalendarModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Pick Date",
                                tint = if (selectedDate.isNotBlank()) AccentOrange else Color(0xFF9E968F),
                                modifier = Modifier.size(20.dp)
                            )
                            if (selectedDate.isNotBlank()) {
                                Text(
                                    text = selectedDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentOrange
                                )
                            }
                        }
                    }

                    // 🚩 Flag / Priority Icon with Popup Menu matching Screenshot_20260920-045341_TickTick.jpg
                    Box {
                        IconButton(onClick = { showPriorityMenu = true }) {
                            val flagTint = when (selectedPriority) {
                                3 -> PriorityHighColor
                                2 -> PriorityMediumColor
                                1 -> Color(0xFF3B82F6) // Blue for Low Priority in TickTick screenshot
                                else -> Color(0xFF9E968F)
                            }
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = "Priority",
                                tint = flagTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Priority Dropdown matching Screenshot 3
                        DropdownMenu(
                            expanded = showPriorityMenu,
                            onDismissRequest = { showPriorityMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF221E1A))
                                .border(1.dp, Color(0xFF332A22), RoundedCornerShape(12.dp))
                        ) {
                            // 🚩 High Priority
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Flag,
                                            contentDescription = null,
                                            tint = PriorityHighColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "High Priority",
                                            color = Color.White,
                                            fontWeight = if (selectedPriority == 3) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPriority = 3
                                    showPriorityMenu = false
                                }
                            )

                            // 🚩 Medium Priority
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Flag,
                                            contentDescription = null,
                                            tint = PriorityMediumColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Medium Priority",
                                            color = Color.White,
                                            fontWeight = if (selectedPriority == 2) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPriority = 2
                                    showPriorityMenu = false
                                }
                            )

                            // 🚩 Low Priority
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Flag,
                                            contentDescription = null,
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Low Priority",
                                            color = Color.White,
                                            fontWeight = if (selectedPriority == 1) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPriority = 1
                                    showPriorityMenu = false
                                }
                            )

                            // 🏳️ No Priority
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Flag,
                                            contentDescription = null,
                                            tint = PriorityNoneColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "No Priority",
                                            color = Color.White,
                                            fontWeight = if (selectedPriority == 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPriority = 0
                                    showPriorityMenu = false
                                }
                            )
                        }
                    }

                    // 🏷️ Category Tag Icon
                    Box {
                        IconButton(onClick = { showCategoryMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Label,
                                contentDescription = "Category",
                                tint = if (category.isNotBlank()) Color(0xFFE8AC70) else Color(0xFF9E968F),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF221E1A))
                                .border(1.dp, Color(0xFF332A22), RoundedCornerShape(12.dp))
                        ) {
                            listOf("Personal", "Work", "Shopping", "Ideas", "Inbox").forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cat,
                                            color = if (category == cat) AccentOrange else Color.White,
                                            fontWeight = if (category == cat) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        category = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 🖼️ Image / Photo
                    IconButton(onClick = { hasPhotoAttached = !hasPhotoAttached }) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Attach Image",
                            tint = if (hasPhotoAttached) AccentOrange else Color(0xFF9E968F),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 📷 Camera
                    IconButton(onClick = { hasPhotoAttached = true }) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Take Photo",
                            tint = Color(0xFF9E968F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Right Submit Button: Orange Circle with White Send Arrow
                val canSubmit = title.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSubmit) AccentOrange else Color(0xFF28221B))
                        .clickable(enabled = canSubmit) {
                            if (isEditing && taskToEdit != null && onUpdateTask != null) {
                                val updated = taskToEdit.copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    dueDate = if (selectedDate.isNotBlank()) selectedDate else "Today",
                                    dueTime = if (selectedTime.isNotBlank()) selectedTime else "11:00 PM",
                                    priority = selectedPriority,
                                    category = category,
                                    reminder = if (selectedReminder != "None") selectedReminder else null,
                                    repeat = if (selectedRepeat != "None") selectedRepeat else null
                                )
                                onUpdateTask(updated)
                            } else {
                                val newTask = Task(
                                    id = UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    description = description.trim(),
                                    dueDate = if (selectedDate.isNotBlank()) selectedDate else "Today",
                                    dueTime = if (selectedTime.isNotBlank()) selectedTime else "11:00 PM",
                                    priority = selectedPriority,
                                    category = category,
                                    isCompleted = false,
                                    reminder = if (selectedReminder != "None") selectedReminder else null,
                                    repeat = if (selectedRepeat != "None") selectedRepeat else null
                                )
                                onSaveTask(newTask)
                            }
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isEditing) "Save Changes" else "Submit Task",
                        tint = if (canSubmit) Color.White else Color(0xFF756F68),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Date Picker Modal when calendar icon is clicked
    if (showCalendarModal) {
        TaskDatePickerModal(
            initialDateStr = selectedDate,
            initialTime = selectedTime,
            initialReminder = selectedReminder,
            initialRepeat = selectedRepeat,
            onDismiss = { showCalendarModal = false },
            onClear = {
                selectedDate = ""
                selectedTime = "None"
                selectedReminder = "None"
                selectedRepeat = "None"
            },
            onConfirm = { dateStr, timeStr, reminder, repeat ->
                selectedDate = dateStr
                selectedTime = timeStr
                if (reminder != null) selectedReminder = reminder
                if (repeat != null) selectedRepeat = repeat
            }
        )
    }
}
