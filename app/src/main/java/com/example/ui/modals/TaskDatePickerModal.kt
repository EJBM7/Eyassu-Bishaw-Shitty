package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerModal(
    initialDateStr: String = "Today",
    initialTime: String = "None",
    initialReminder: String = "None",
    initialRepeat: String = "None",
    onDismiss: () -> Unit,
    onClear: () -> Unit = {},
    onConfirm: (dateStr: String, timeStr: String, reminder: String?, repeat: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentCal = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH)) } // 0-based
    var selectedDay by remember { mutableIntStateOf(currentCal.get(Calendar.DAY_OF_MONTH)) }

    var selectedTime by remember { mutableStateOf(if (initialTime.isNotBlank() && initialTime != "None") initialTime else "None") }
    var selectedReminder by remember { mutableStateOf(if (initialReminder.isNotBlank()) initialReminder else "None") }
    var selectedRepeat by remember { mutableStateOf(if (initialRepeat.isNotBlank()) initialRepeat else "None") }

    var activeTab by remember { mutableStateOf("Date") } // Date or Duration
    var durationMinutes by remember { mutableIntStateOf(30) }

    var showTimePickerOptions by remember { mutableStateOf(false) }
    var showReminderOptions by remember { mutableStateOf(false) }
    var showRepeatOptions by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Compute calendar for month
    val monthCal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOfWeek = (monthCal.get(Calendar.DAY_OF_WEEK) - 1) // 0 for Sunday
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFF141210),
        tonalElevation = 6.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: ✕ [Date | Duration] ✓ matching Screenshot_20260920-044934_TickTick.jpg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { activeTab = "Date" }
                    ) {
                        Text(
                            text = "Date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "Date") Color(0xFF00BCD4) else Color(0xFF9E968F)
                        )
                        if (activeTab == "Date") {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .width(36.dp)
                                    .height(2.5.dp)
                                    .background(Color(0xFF00BCD4), CircleShape)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { activeTab = "Duration" }
                    ) {
                        Text(
                            text = "Duration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "Duration") Color(0xFF00BCD4) else Color(0xFF9E968F)
                        )
                        if (activeTab == "Duration") {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .width(44.dp)
                                    .height(2.5.dp)
                                    .background(Color(0xFF00BCD4), CircleShape)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val formattedDate = "${monthNames[month].substring(0, 3)} $selectedDay"
                        onConfirm(formattedDate, selectedTime, selectedReminder, selectedRepeat)
                        onDismiss()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == "Date") {
                // Month Header with < > buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthNames[month],
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                if (month == 0) {
                                    month = 11
                                    year--
                                } else {
                                    month--
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = Color(0xFFD1D5DB)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (month == 11) {
                                    month = 0
                                    year++
                                } else {
                                    month++
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Next Month",
                                tint = Color(0xFFD1D5DB)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weekday Row: Sun Mon Tue Wed Thu Fri Sat
                val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { dayName ->
                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF756F68)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calendar Grid
                val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
                val numRows = totalCells / 7

                for (rowIndex in 0 until numRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val isSelected = (dayNumber == selectedDay)
                                val isToday = (dayNumber == currentCal.get(Calendar.DAY_OF_MONTH) &&
                                        month == currentCal.get(Calendar.MONTH) &&
                                        year == currentCal.get(Calendar.YEAR))

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF00BCD4) // Teal highlight matching TickTick screenshot
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (isToday && !isSelected) {
                                                Modifier.border(1.dp, Color(0xFF00BCD4), CircleShape)
                                            } else Modifier
                                        )
                                        .clickable { selectedDay = dayNumber },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> Color(0xFF00BCD4)
                                            else -> Color(0xFFD6D1CA)
                                        }
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            } else {
                // Duration Tab View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estimated Duration",
                        fontSize = 15.sp,
                        color = Color(0xFF9E968F)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$durationMinutes min",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(15, 30, 45, 60, 90, 120).forEach { mins ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (durationMinutes == mins) Color(0xFF00BCD4) else Color(0xFF221E19),
                                modifier = Modifier.clickable { durationMinutes = mins }
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (durationMinutes == mins) Color.White else Color(0xFFD1D5DB),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options Container (Time, Reminder, Repeat) matching Screenshot_20260920-044934_TickTick.jpg
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1B1713),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26201A), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    // 🕒 Time Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePickerOptions = !showTimePickerOptions }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Time",
                                tint = Color(0xFF9E968F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Time",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedTime,
                                fontSize = 14.sp,
                                color = if (selectedTime != "None") Color(0xFF00BCD4) else Color(0xFF756F68)
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF756F68),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Time quick selector
                    AnimatedVisibility(visible = showTimePickerOptions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("None", "09:00 AM", "02:00 PM", "07:00 PM", "11:00 PM").forEach { t ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedTime == t) Color(0xFF00BCD4) else Color(0xFF29241E),
                                    modifier = Modifier.clickable {
                                        selectedTime = t
                                        showTimePickerOptions = false
                                    }
                                ) {
                                    Text(
                                        text = t,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedTime == t) Color.White else Color(0xFFD1D5DB),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0xFF26201A))
                    )

                    // ⏰ Reminder Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReminderOptions = !showReminderOptions }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Reminder",
                                tint = Color(0xFF9E968F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Reminder",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedReminder,
                                fontSize = 14.sp,
                                color = if (selectedReminder != "None") Color(0xFF00BCD4) else Color(0xFF756F68)
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF756F68),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Reminder quick selector
                    AnimatedVisibility(visible = showReminderOptions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("None", "On time", "5m before", "30m before", "1d before").forEach { r ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedReminder == r) Color(0xFF00BCD4) else Color(0xFF29241E),
                                    modifier = Modifier.clickable {
                                        selectedReminder = r
                                        showReminderOptions = false
                                    }
                                ) {
                                    Text(
                                        text = r,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedReminder == r) Color.White else Color(0xFFD1D5DB),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0xFF26201A))
                    )

                    // 🔁 Repeat Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRepeatOptions = !showRepeatOptions }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Repeat,
                                contentDescription = "Repeat",
                                tint = Color(0xFF9E968F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Repeat",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedRepeat,
                                fontSize = 14.sp,
                                color = if (selectedRepeat != "None") Color(0xFF00BCD4) else Color(0xFF756F68)
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF756F68),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Repeat quick selector
                    AnimatedVisibility(visible = showRepeatOptions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("None", "Daily", "Weekdays", "Weekly", "Monthly").forEach { rep ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedRepeat == rep) Color(0xFF00BCD4) else Color(0xFF29241E),
                                    modifier = Modifier.clickable {
                                        selectedRepeat = rep
                                        showRepeatOptions = false
                                    }
                                ) {
                                    Text(
                                        text = rep,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedRepeat == rep) Color.White else Color(0xFFD1D5DB),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Clear button in red text, matching Screenshot_20260920-044934_TickTick.jpg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        selectedTime = "None"
                        selectedReminder = "None"
                        selectedRepeat = "None"
                        onClear()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Clear",
                        color = Color(0xFFEF4444),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
