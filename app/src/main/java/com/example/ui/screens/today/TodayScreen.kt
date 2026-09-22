package com.example.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.ui.components.HabitIcon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.model.AppSettings
import com.example.model.Habit
import com.example.model.MainTab
import com.example.model.Task
import com.example.model.TimeRangeView
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.HabitProgressSquareButton
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.drawBehind
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentOrangeLight
import com.example.ui.theme.CardShape
import com.example.ui.theme.CategoryBadgeBg
import com.example.ui.theme.CategoryBadgeText
import com.example.ui.theme.InnerCardShape
import com.example.ui.theme.PillShape

@Composable
fun TodayScreen(
    habits: List<Habit>,
    tasks: List<Task>,
    settings: AppSettings = AppSettings(),
    onToggleHabit: (String) -> Unit,
    onIncrementHabit: (String) -> Unit,
    onToggleDate: (String, String) -> Unit = { _, _ -> },
    onHabitClick: (Habit) -> Unit,
    onToggleTask: (String) -> Unit,
    onFocusClick: () -> Unit,
    onNotesClick: () -> Unit,
    onViewAllTasks: () -> Unit,
    onNewHabitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTimeRange by remember { mutableStateOf(TimeRangeView.WEEK) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(habits) {
        listOf("All") + habits.map { it.category }.distinct()
    }

    val filteredHabits = remember(habits, selectedCategory) {
        if (selectedCategory == "All") habits else habits.filter { it.category == selectedCategory }
    }

    val completedHabitsCount = habits.count { it.isCompletedToday }
    val totalHabitsCount = habits.size
    val progressFraction = if (totalHabitsCount > 0) completedHabitsCount.toFloat() / totalHabitsCount else 0f
    val progressPercent = (progressFraction * 100).toInt()

    val accentColor = remember(settings.accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(settings.accentColorHex))
        } catch (_: Exception) {
            AccentOrange
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top Header
            item {
                TodayHeader(
                    onFocusClick = onFocusClick,
                    onNotesClick = onNotesClick,
                    onNewHabitClick = onNewHabitClick
                )
            }

            // Date & Progress Ring Card
            item {
                DateProgressCard(
                    completedCount = completedHabitsCount,
                    totalCount = totalHabitsCount,
                    percent = progressPercent,
                    fraction = progressFraction,
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Time Range Segmented Control (Week, Month, Year)
            item {
                TimeRangeControl(
                    selectedRange = selectedTimeRange,
                    onSelectRange = { selectedTimeRange = it },
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Category Filter Pills
            item {
                CategoryPillsRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Habit Cards
            items(filteredHabits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    timeRange = selectedTimeRange,
                    checkStyle = settings.checkStyle,
                    weekStartsOnMonday = settings.weekStartsOnMonday,
                    onToggle = { onToggleHabit(habit.id) },
                    onIncrement = { onIncrementHabit(habit.id) },
                    onToggleDate = { dateStr -> onToggleDate(habit.id, dateStr) },
                    onClick = { onHabitClick(habit) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Today's Tasks Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                TodayTasksSection(
                    tasks = tasks,
                    onToggleTask = onToggleTask,
                    onViewAll = onViewAllTasks,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun TodayHeader(
    onFocusClick: () -> Unit,
    onNotesClick: () -> Unit,
    onNewHabitClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Today",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onNotesClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = "Notes",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Focus pill button
            Surface(
                shape = PillShape,
                color = Color(0xFF16161C),
                modifier = Modifier
                    .clip(PillShape)
                    .clickable(onClick = onFocusClick)
                    .border(1.dp, Color(0xFF262630), PillShape)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Focus",
                        tint = AccentOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Focus",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // + New glowing pill button
            Surface(
                shape = PillShape,
                color = Color(0xFF0084FF),
                modifier = Modifier
                    .clip(PillShape)
                    .shadow(
                        elevation = 6.dp,
                        shape = PillShape,
                        ambientColor = Color(0xFF0084FF).copy(alpha = 0.5f),
                        spotColor = Color(0xFF0084FF)
                    )
                    .clickable(onClick = onNewHabitClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New Habit",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DateProgressCard(
    completedCount: Int,
    totalCount: Int,
    percent: Int,
    fraction: Float,
    accentColor: Color
) {
    val dateText = remember {
        SimpleDateFormat("EEEE, d MMM", Locale.US).format(Date())
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(0.4f),
                spotColor = Color.Black.copy(0.5f)
            )
            .border(1.dp, Color(0xFF2E2E3C), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = dateText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF)
                )
                Text(
                    text = "$completedCount of $totalCount completed",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            CircularProgressRing(
                progress = fraction,
                size = 62.dp,
                strokeWidth = 6.dp,
                trackColor = Color(0xFF222228),
                gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF0084FF)),
                centerText = "$percent%"
            )
        }
    }
}

@Composable
private fun TimeRangeControl(
    selectedRange: TimeRangeView,
    onSelectRange: (TimeRangeView) -> Unit,
    accentColor: Color
) {
    Surface(
        shape = PillShape,
        color = Color(0xFF161310),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), PillShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeRangeItem("Week", selectedRange == TimeRangeView.WEEK, accentColor) { onSelectRange(TimeRangeView.WEEK) }
            TimeRangeItem("Month", selectedRange == TimeRangeView.MONTH, accentColor) { onSelectRange(TimeRangeView.MONTH) }
            TimeRangeItem("Year", selectedRange == TimeRangeView.YEAR, accentColor) { onSelectRange(TimeRangeView.YEAR) }
        }
    }
}

@Composable
private fun TimeRangeItem(
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Transparent,
        label = "range_bg"
    )
    val textColor = if (isSelected) Color.White else Color(0xFF9CA3AF)

    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CategoryPillsRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat == selectedCategory
            val bg = if (isSelected) accentColor else Color(0xFF161310)
            val textColor = if (isSelected) Color.White else Color(0xFF9E968F)

            Surface(
                shape = PillShape,
                color = bg,
                modifier = Modifier
                    .clip(PillShape)
                    .clickable { onSelectCategory(cat) }
                    .border(1.dp, if (isSelected) accentColor else Color(0xFF2B231C), PillShape)
            ) {
                Text(
                    text = cat,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    timeRange: TimeRangeView,
    checkStyle: String = "Square",
    weekStartsOnMonday: Boolean = true,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onToggleDate: (String) -> Unit = {},
    onClick: () -> Unit
) {
    val habitColor = runCatching {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    }.getOrDefault(AccentOrange)
    val isComplete = habit.isCompletedToday
    val progress = if (habit.targetCount > 0) {
        (habit.currentCountToday.toFloat() / habit.targetCount).coerceIn(0f, 1f)
    } else if (habit.isCompletedToday) 1f else 0f

    val isMultiStep = habit.targetCount > 1

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF2E2E3C), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Icon + Title + Streak + Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category/Habit Icon with rounded glowing container
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(habitColor.copy(alpha = 0.16f))
                            .border(1.2.dp, habitColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        HabitIcon(
                            iconName = habit.iconName,
                            tint = habitColor,
                            size = 22.dp
                        )
                    }

                    // Title & Dynamic Streak & Count info
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = habit.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (habit.streakDays > 0) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${habit.streakDays} · ",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            if (isMultiStep) {
                                Text(
                                    text = "${habit.currentCountToday}/${habit.targetCount} ${habit.unit} · ",
                                    fontSize = 12.sp,
                                    fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isComplete) habitColor else Color.White
                                )
                            }
                            Text(
                                text = habit.category,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // Action button: Squircle that traces perimeter during progress and fills at completion!
                HabitProgressSquareButton(
                    habitColor = habitColor,
                    isComplete = isComplete,
                    progress = progress,
                    isMultiStep = isMultiStep,
                    onClick = if (isMultiStep && !isComplete) onIncrement else onToggle,
                    size = 46.dp,
                    cornerRadius = 14.dp,
                    testTag = "habit_action_${habit.id}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Glowing linear progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(PillShape)
                        .background(Color(0xFF222228))
                ) {
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                                .fillMaxSize()
                                .clip(PillShape)
                                .background(habitColor)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = PillShape,
                                    ambientColor = habitColor.copy(alpha = 0.5f),
                                    spotColor = habitColor
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                val displayPercent = "${(progress * 100).toInt()}%"
                Text(
                    text = displayPercent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Days view (Week, Month, Year)
            when (timeRange) {
                TimeRangeView.WEEK -> WeekDayPills(
                    habit = habit,
                    habitColor = habitColor,
                    weekStartsOnMonday = weekStartsOnMonday,
                    onToggleDate = onToggleDate
                )
                TimeRangeView.MONTH -> MonthHeatmapGrid(
                    habit = habit,
                    habitColor = habitColor,
                    onToggleDate = onToggleDate
                )
                TimeRangeView.YEAR -> YearMatrixHeatmap(habit = habit, habitColor = habitColor)
            }
        }
    }
}

@Composable
private fun WeekDayPills(
    habit: Habit,
    habitColor: Color,
    weekStartsOnMonday: Boolean = true,
    onToggleDate: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayNameFormat = remember { SimpleDateFormat("EEE", Locale.US) }
    val dayNumFormat = remember { SimpleDateFormat("d", Locale.US) }
    val todayStr = remember { dateFormat.format(Date()) }

    val days = remember(weekStartsOnMonday) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = if (weekStartsOnMonday) Calendar.MONDAY else Calendar.SUNDAY
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val firstDay = cal.firstDayOfWeek
        var diff = dayOfWeek - firstDay
        if (diff < 0) diff += 7
        cal.add(Calendar.DAY_OF_MONTH, -diff)

        val list = mutableListOf<Triple<String, String, String>>() // dayName, dayNum, fullDate
        for (i in 0 until 7) {
            val d = cal.time
            list.add(Triple(dayNameFormat.format(d), dayNumFormat.format(d), dateFormat.format(d)))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (dayName, dayNum, fullDate) ->
            val isDone = habit.completedDates.contains(fullDate)
            val isToday = fullDate == todayStr

            val pillShape = RoundedCornerShape(12.dp)
            val onDoneTextColor = if (habitColor.luminance() > 0.65f) Color.Black else Color.White

            Surface(
                shape = pillShape,
                color = if (isDone) habitColor else Color(0xFF16161C),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isToday) 1.5.dp else 1.dp,
                    color = if (isToday) habitColor else if (isDone) habitColor else Color(0xFF262630)
                ),
                modifier = Modifier
                    .width(40.dp)
                    .height(52.dp)
                    .shadow(
                        elevation = if (isDone) 6.dp else 0.dp,
                        shape = pillShape,
                        ambientColor = habitColor.copy(alpha = 0.5f),
                        spotColor = habitColor
                    )
                    .clip(pillShape)
                    .clickable { onToggleDate(fullDate) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayNum,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) onDoneTextColor else Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dayName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) onDoneTextColor.copy(alpha = 0.85f) else if (isToday) habitColor else Color(0xFF8A827B)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeatmapGrid(
    habit: Habit,
    habitColor: Color,
    onToggleDate: (String) -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    val year = remember { cal.get(Calendar.YEAR) }
    val month = remember { cal.get(Calendar.MONTH) + 1 }
    val daysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val monthName = remember { SimpleDateFormat("MMM", Locale.US).format(cal.time) }
    val monthPrefix = remember { String.format("%04d-%02d", year, month) }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    val doneInMonth = habit.completedDates.count { it.startsWith(monthPrefix) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$monthName Heatmap",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$doneInMonth / $daysInMonth days",
                fontSize = 12.sp,
                color = habitColor,
                fontWeight = FontWeight.Bold
            )
        }

        val rows = (daysInMonth + 9) / 10
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 1..10) {
                    val dayNum = row * 10 + col
                    if (dayNum <= daysInMonth) {
                        val dateKey = String.format("%04d-%02d-%02d", year, month, dayNum)
                        val isDone = habit.completedDates.contains(dateKey)
                        val isToday = dateKey == todayDateStr

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDone) habitColor else Color(0xFF1E1E24))
                                .clickable { onToggleDate(dateKey) }
                                .border(
                                    width = if (isToday) 1.5.dp else 0.5.dp,
                                    color = if (isToday) Color.White else Color(0xFF2A2A32),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 9.sp,
                                color = if (isDone) Color.Black else Color(0xFF9CA3AF),
                                fontWeight = if (isDone || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun YearMatrixHeatmap(
    habit: Habit,
    habitColor: Color
) {
    val months = listOf("May", "Jun", "Jul", "Aug", "Sep")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach { m ->
                Text(
                    text = m,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Continuous 7 rows x 22 columns heatmap grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (col in 0 until 22) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (row in 0 until 7) {
                        val isFilled = when {
                            habit.id == "habit_2" -> {
                                when (col) {
                                    18 -> row in 0..5
                                    19 -> row in 0..6
                                    20 -> row in 0..4
                                    21 -> row in 0..2 && habit.isCompletedToday
                                    else -> false
                                }
                            }
                            habit.id == "habit_1" -> {
                                when (col) {
                                    19 -> row in 0..5
                                    else -> false
                                }
                            }
                            else -> {
                                val dayNum = (col - 17) * 7 + row + 1
                                if (col >= 17 && dayNum in 1..30) {
                                    val dayStr = if (dayNum < 10) "0$dayNum" else "$dayNum"
                                    habit.completedDates.contains("2026-09-$dayStr")
                                } else false
                            }
                        }

                        val cellColor = if (isFilled) {
                            if (habit.id == "habit_1" && col == 19 && row in 0..2) {
                                Color(0xFF3B82F6) // Cyan/blue
                            } else if (habit.id == "habit_1" && col == 19) {
                                Color(0xFFEC4899) // Pink/magenta
                            } else {
                                habitColor
                            }
                        } else {
                            Color(0xFF22222A)
                        }

                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(cellColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTasksSection(
    tasks: List<Task>,
    onToggleTask: (String) -> Unit,
    onViewAll: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Tasks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "View All (${tasks.size})",
                fontSize = 13.sp,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }

        if (tasks.isEmpty()) {
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E2E3C), CardShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks",
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tasks.take(4).forEach { task ->
                    TaskItemCard(task = task, onToggle = { onToggleTask(task.id) }, accentColor = accentColor)
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggle: () -> Unit,
    accentColor: Color = AccentOrange
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(0.4f),
                spotColor = Color.Black.copy(0.5f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .border(1.dp, Color(0xFF2E2E3C), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = "Toggle task",
                    tint = if (task.isCompleted) accentColor else Color(0xFF6B7280),
                    modifier = Modifier.size(22.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) Color(0xFF6B7280) else Color.White,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )

                    Text(
                        text = "Today · ${task.dueTime}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Category tag
            Surface(
                shape = PillShape,
                color = CategoryBadgeBg,
                modifier = Modifier.clip(PillShape)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentOrange)
                    )
                    Text(
                        text = task.category,
                        fontSize = 11.sp,
                        color = CategoryBadgeText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
