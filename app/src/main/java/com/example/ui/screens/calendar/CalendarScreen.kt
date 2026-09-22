package com.example.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Habit
import com.example.model.Task
import com.example.ui.components.HabitIcon
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.CardShape
import com.example.ui.theme.CategoryBadgeText
import com.example.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CalendarViewMode {
    MONTH, WEEK
}

enum class DayFilter {
    ALL, HABITS, TASKS
}

@Composable
fun CalendarScreen(
    habits: List<Habit>,
    tasks: List<Task>,
    onToggleHabitForDate: (String, String) -> Unit, // habitId, dateStr
    onHabitClick: (Habit) -> Unit,
    onToggleTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onNewHabitClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCal = remember { Calendar.getInstance() }
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(todayCal.time)
    }

    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val selectedDateStr = remember(selectedCalendar) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)
    }

    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var selectedFilter by remember { mutableStateOf(DayFilter.ALL) }

    // Navigation between months
    fun changeMonth(offset: Int) {
        val newCal = selectedCalendar.clone() as Calendar
        newCal.add(Calendar.MONTH, offset)
        selectedCalendar = newCal
    }

    fun jumpToToday() {
        selectedCalendar = Calendar.getInstance()
    }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayHeaderFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }

    // Filtered items for selected day
    val habitsForDay = habits
    val completedHabitsCount = habits.count { it.completedDates.contains(selectedDateStr) }
    val completedTasksCount = tasks.count { it.isCompleted }
    val totalItems = habits.size + tasks.size
    val totalCompleted = completedHabitsCount + completedTasksCount

    // Timeline Unified Items Construction
    val timelineItems = remember(habits, tasks, selectedFilter, selectedDateStr) {
        val list = mutableListOf<TimelineItem>()

        if (selectedFilter != DayFilter.TASKS) {
            habits.forEach { habit ->
                val isChecked = habit.completedDates.contains(selectedDateStr)
                val timeStr = habit.reminderTime ?: when (habit.timeOfDay) {
                    "Morning 🌅", "Morning" -> "8:00 AM"
                    "Afternoon ☀️", "Afternoon" -> "1:00 PM"
                    "Evening 🌇", "Evening" -> "6:00 PM"
                    "Night 🌙", "Night" -> "9:00 PM"
                    else -> "All day"
                }
                val sortKey = when (timeStr) {
                    "All day" -> 0
                    "8:00 AM" -> 800
                    "1:00 PM" -> 1300
                    "6:00 PM" -> 1800
                    "9:00 PM" -> 2100
                    else -> 1200
                }
                list.add(
                    TimelineItem.HabitItem(
                        habit = habit,
                        isChecked = isChecked,
                        timeDisplay = timeStr,
                        sortOrder = sortKey
                    )
                )
            }
        }

        if (selectedFilter != DayFilter.HABITS) {
            tasks.forEach { task ->
                val timeStr = if (task.dueTime.isNotBlank()) task.dueTime else "All day"
                val sortKey = if (timeStr.contains("AM", ignoreCase = true)) {
                    900
                } else if (timeStr.contains("PM", ignoreCase = true)) {
                    1900
                } else {
                    50
                }
                list.add(
                    TimelineItem.TaskItem(
                        task = task,
                        timeDisplay = timeStr,
                        sortOrder = sortKey
                    )
                )
            }
        }

        list.sortedBy { it.sortOrder }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Top Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Title with prev / next navigation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { changeMonth(-1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = monthYearFormat.format(selectedCalendar.time),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { changeMonth(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = Color.White
                        )
                    }
                }

                // Right Action Controls: View mode switcher & Jump to Today
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Today Pill
                    Surface(
                        shape = PillShape,
                        color = if (selectedDateStr == todayDateStr) AccentOrange else Color(0xFF1D1712),
                        modifier = Modifier
                            .clip(PillShape)
                            .clickable { jumpToToday() }
                            .border(1.dp, Color(0xFF332920), PillShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Today",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Month/Week switcher icon button
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == CalendarViewMode.MONTH) {
                                CalendarViewMode.WEEK
                            } else {
                                CalendarViewMode.MONTH
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1D1712), CircleShape)
                            .border(1.dp, Color(0xFF332920), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (viewMode == CalendarViewMode.MONTH) {
                                Icons.Default.CalendarViewWeek
                            } else {
                                Icons.Default.CalendarMonth
                            },
                            contentDescription = "Switch View Mode",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Calendar Grid Container (Obsidian card style with subtle shadow)
        item {
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = CardShape, spotColor = Color.Black)
                    .border(1.dp, Color(0xFF2B231C), CardShape)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (viewMode == CalendarViewMode.MONTH) {
                        MonthCalendarGrid(
                            currentCalendar = selectedCalendar,
                            selectedDateStr = selectedDateStr,
                            todayDateStr = todayDateStr,
                            habits = habits,
                            onDateSelected = { clickedCal ->
                                selectedCalendar = clickedCal
                            }
                        )
                    } else {
                        WeekStripView(
                            currentCalendar = selectedCalendar,
                            selectedDateStr = selectedDateStr,
                            todayDateStr = todayDateStr,
                            habits = habits,
                            onDateSelected = { clickedCal ->
                                selectedCalendar = clickedCal
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Day Section Header: Filter Chips & Add Action (Unwanted text removed)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips: All, Habits, Tasks
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        label = "All",
                        isSelected = selectedFilter == DayFilter.ALL,
                        onClick = { selectedFilter = DayFilter.ALL }
                    )
                    FilterChip(
                        label = "Habits",
                        isSelected = selectedFilter == DayFilter.HABITS,
                        onClick = { selectedFilter = DayFilter.HABITS }
                    )
                    FilterChip(
                        label = "Tasks",
                        isSelected = selectedFilter == DayFilter.TASKS,
                        onClick = { selectedFilter = DayFilter.TASKS }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedFilter != DayFilter.TASKS) {
                        Text(
                            text = "+ Habit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentOrange,
                            modifier = Modifier.clickable { onNewHabitClick() }
                        )
                    }
                    if (selectedFilter != DayFilter.HABITS) {
                        Text(
                            text = "+ Task",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentOrange,
                            modifier = Modifier.clickable { onNewTaskClick() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (timelineItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181822)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF282836), RoundedCornerShape(16.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items scheduled for this day",
                            color = Color(0xFF8E8E93),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(timelineItems.size, key = { timelineItems[it].id }) { index ->
                val item = timelineItems[index]
                val isFirst = index == 0
                val isLast = index == timelineItems.size - 1

                TimelineRowItem(
                    item = item,
                    isFirst = isFirst,
                    isLast = isLast,
                    onToggleHabit = { habitId ->
                        onToggleHabitForDate(habitId, selectedDateStr)
                    },
                    onHabitClick = { habit ->
                        onHabitClick(habit)
                    },
                    onToggleTask = { taskId ->
                        onToggleTask(taskId)
                    },
                    onEditTask = { task ->
                        onEditTask(task)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = PillShape,
        color = if (isSelected) AccentOrange else Color(0xFF161310),
        modifier = Modifier
            .clip(PillShape)
            .clickable { onClick() }
            .border(1.dp, if (isSelected) AccentOrange else Color(0xFF2B231C), PillShape)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun MonthCalendarGrid(
    currentCalendar: Calendar,
    selectedDateStr: String,
    todayDateStr: String,
    habits: List<Habit>,
    onDateSelected: (Calendar) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val cal = currentCalendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH)
    val year = cal.get(Calendar.YEAR)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Weekday Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayName ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }

        // Calendar Days (up to 6 rows)
        var currentDay = 1
        var startOffset = firstDayOfWeek - 1 // 0-based offset for Sunday

        for (row in 0 until 6) {
            if (currentDay > maxDaysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    if (row == 0 && col < startOffset) {
                        // Empty slot before month start
                        Box(modifier = Modifier.weight(1f))
                    } else if (currentDay <= maxDaysInMonth) {
                        val dayNumber = currentDay
                        val dayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                        }
                        val dayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCal.time)
                        val isSelected = (dayStr == selectedDateStr)
                        val isToday = (dayStr == todayDateStr)

                        // Habits completed on this date
                        val completedCount = habits.count { it.completedDates.contains(dayStr) }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> AccentOrange
                                        isToday -> Color(0xFF2B2016)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.dp else 0.dp,
                                    color = if (isToday && !isSelected) AccentOrange else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onDateSelected(dayCal) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> AccentOrange
                                        else -> Color.White
                                    }
                                )

                                // Dots indicating completed habits/activities
                                if (completedCount > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        repeat(minOf(completedCount, 3)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        if (isSelected) Color.White else AccentAmber,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        currentDay++
                    } else {
                        // Empty slot after month end
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekStripView(
    currentCalendar: Calendar,
    selectedDateStr: String,
    todayDateStr: String,
    habits: List<Habit>,
    onDateSelected: (Calendar) -> Unit
) {
    // 7 days around selected date
    val weekCal = currentCalendar.clone() as Calendar
    weekCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

    val weekDays = (0 until 7).map {
        val c = weekCal.clone() as Calendar
        c.add(Calendar.DAY_OF_WEEK, it)
        c
    }

    val dayNameFormat = remember { SimpleDateFormat("EEE", Locale.US) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { dayCal ->
            val dayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCal.time)
            val isSelected = (dayStr == selectedDateStr)
            val isToday = (dayStr == todayDateStr)
            val completedCount = habits.count { it.completedDates.contains(dayStr) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            isSelected -> AccentOrange
                            isToday -> Color(0xFF2B2016)
                            else -> Color(0xFF161310)
                        }
                    )
                    .border(
                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                        color = if (isToday && !isSelected) AccentOrange else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onDateSelected(dayCal) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dayNameFormat.format(dayCal.time),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF9CA3AF)
                    )

                    Text(
                        text = "${dayCal.get(Calendar.DAY_OF_MONTH)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (completedCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(if (isSelected) Color.White else AccentAmber, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

sealed class TimelineItem {
    abstract val id: String
    abstract val timeDisplay: String
    abstract val sortOrder: Int

    data class HabitItem(
        val habit: Habit,
        val isChecked: Boolean,
        override val timeDisplay: String,
        override val sortOrder: Int
    ) : TimelineItem() {
        override val id: String = "habit_${habit.id}"
    }

    data class TaskItem(
        val task: Task,
        override val timeDisplay: String,
        override val sortOrder: Int
    ) : TimelineItem() {
        override val id: String = "task_${task.id}"
    }
}

@Composable
private fun TimelineRowItem(
    item: TimelineItem,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleHabit: (String) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onToggleTask: (String) -> Unit,
    onEditTask: (Task) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Time label
        Box(
            modifier = Modifier
                .width(52.dp)
                .padding(end = 6.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = item.timeDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9E968F)
            )
        }

        // Center Column: Timeline Line with Node
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Continuous vertical line behind the node
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .align(Alignment.TopCenter)
                        .background(Color(0xFF2E2E3E))
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF2E2E3E))
                )
            }

            // Node marker on the line
            when (item) {
                is TimelineItem.HabitItem -> {
                    val habitColor = runCatching {
                        Color(android.graphics.Color.parseColor(item.habit.colorHex))
                    }.getOrDefault(AccentOrange)

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (item.isChecked) habitColor else Color(0xFF14141B))
                            .border(2.dp, habitColor, CircleShape)
                    )
                }
                is TimelineItem.TaskItem -> {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (item.task.isCompleted) AccentOrange else Color(0xFF14141B))
                            .border(2.dp, if (item.task.isCompleted) AccentOrange else Color(0xFF5E5E72), CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right Column: Elevated Card with Checkbox in front
        when (item) {
            is TimelineItem.HabitItem -> {
                val habit = item.habit
                val isChecked = item.isChecked
                val habitColor = runCatching {
                    Color(android.graphics.Color.parseColor(habit.colorHex))
                }.getOrDefault(AccentOrange)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(14.dp), spotColor = Color.Black)
                        .border(
                            1.dp,
                            if (isChecked) habitColor.copy(alpha = 0.5f) else Color(0xFF2E2E3C),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onHabitClick(habit) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Checkbox in front of the habit
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isChecked) habitColor else Color(0xFF16161E))
                                .border(
                                    width = if (isChecked) 0.dp else 1.5.dp,
                                    color = if (isChecked) Color.Transparent else Color(0xFF444456),
                                    shape = RoundedCornerShape(7.dp)
                                )
                                .clickable { onToggleHabit(habit.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Habit Icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(habitColor.copy(alpha = 0.2f))
                                .border(1.dp, habitColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            HabitIcon(iconName = habit.iconName, tint = habitColor, size = 18.dp)
                        }

                        // Title & Subtitle
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = habit.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "${habit.targetCount} ${habit.unit}/day",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E968F)
                                )

                                if (habit.streakDays > 0) {
                                    Text(text = "·", fontSize = 11.sp, color = Color(0xFF6B7280))
                                    Text(
                                        text = "🔥 ${habit.streakDays}d",
                                        fontSize = 11.sp,
                                        color = AccentAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is TimelineItem.TaskItem -> {
                val task = item.task

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(14.dp), spotColor = Color.Black)
                        .border(
                            1.dp,
                            if (task.isCompleted) AccentOrange.copy(alpha = 0.4f) else Color(0xFF2E2E3C),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onEditTask(task) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Checkbox in front of the task
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (task.isCompleted) AccentOrange else Color(0xFF16161E))
                                .border(
                                    width = if (task.isCompleted) 0.dp else 1.5.dp,
                                    color = if (task.isCompleted) Color.Transparent else Color(0xFF444456),
                                    shape = RoundedCornerShape(7.dp)
                                )
                                .clickable { onToggleTask(task.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Task Title & Category
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.isCompleted) Color(0xFF6B7280) else Color.White,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = task.category,
                                    fontSize = 11.sp,
                                    color = CategoryBadgeText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
