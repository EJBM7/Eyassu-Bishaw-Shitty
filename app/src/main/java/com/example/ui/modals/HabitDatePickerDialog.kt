package com.example.ui.modals

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentOrange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitDatePickerDialog(
    currentDateStr: String,
    accentColorHex: String = "#FF6D00",
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val activeColor = remember(accentColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(accentColorHex)) }.getOrDefault(AccentOrange)
    }

    val cal = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) } // 0-based
    var selectedDay by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Compute month calendar info
    val monthCal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
    val maxDays = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val todayCal = remember { Calendar.getInstance() }
    val isCurrentMonthToday = (year == todayCal.get(Calendar.YEAR) && month == todayCal.get(Calendar.MONTH))
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF16161F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E3E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Start Date",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9E968F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month / Year Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E28))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (month == 0) {
                                month = 11
                                year--
                            } else {
                                month--
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "${monthNames[month]} $year",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            if (month == 11) {
                                month = 0
                                year++
                            } else {
                                month++
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day-of-week headers
                val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { d ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = d,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E8E98)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid (up to 6 rows)
                var currentDay = 1
                val startOffset = firstDayOfWeek - 1 // 0-based for Sunday

                for (row in 0 until 6) {
                    if (currentDay > maxDays) break
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            if (row == 0 && col < startOffset) {
                                Box(modifier = Modifier.weight(1f))
                            } else if (currentDay <= maxDays) {
                                val d = currentDay
                                val isSelected = (d == selectedDay)
                                val isToday = (isCurrentMonthToday && d == todayDay)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> activeColor
                                                isToday -> activeColor.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = if (isToday && !isSelected) activeColor else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedDay = d },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$d",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> activeColor
                                            else -> Color.White
                                        }
                                    )
                                }
                                currentDay++
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Presets: "Today", "Tomorrow", "Next Monday"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Today" to 0,
                        "Tomorrow" to 1,
                        "Next Mon" to 7
                    ).forEach { (label, dayOffset) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E1E28),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E3E)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val quickCal = Calendar.getInstance().apply {
                                        if (dayOffset == 7) {
                                            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                                                add(Calendar.DAY_OF_YEAR, 1)
                                            }
                                        } else {
                                            add(Calendar.DAY_OF_YEAR, dayOffset)
                                        }
                                    }
                                    year = quickCal.get(Calendar.YEAR)
                                    month = quickCal.get(Calendar.MONTH)
                                    selectedDay = quickCal.get(Calendar.DAY_OF_MONTH)
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD6D1CA)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF9E968F), fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            val selCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, selectedDay)
                            }
                            val formatted = if (isCurrentMonthToday && selectedDay == todayDay) {
                                "Today"
                            } else {
                                SimpleDateFormat("MMM d, yyyy", Locale.US).format(selCal.time)
                            }
                            onDateSelected(formatted)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                    ) {
                        Text("Set Date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
