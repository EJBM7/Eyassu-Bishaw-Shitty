package com.example.ui.screens.analysis

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Habit
import com.example.ui.components.CircularProgressRing
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentOrangeLight
import com.example.ui.theme.CardShape
import com.example.ui.theme.InnerCardShape
import com.example.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalysisScreen(
    habits: List<Habit>,
    modifier: Modifier = Modifier
) {
    val allLabel = "All Habits (${habits.size})"
    var selectedFilter by remember { mutableStateOf(allLabel) }

    val isAllSelected = selectedFilter.startsWith("All Habits") || habits.none { it.name == selectedFilter }
    val targetHabits = if (isAllSelected) habits else habits.filter { it.name == selectedFilter }
    val selectedHabit = if (!isAllSelected) targetHabits.firstOrNull() else null

    val accentColor = remember(selectedHabit) {
        if (selectedHabit != null) {
            try {
                Color(android.graphics.Color.parseColor(selectedHabit.colorHex))
            } catch (e: Exception) {
                AccentOrange
            }
        } else {
            AccentOrange
        }
    }

    val cal = remember { Calendar.getInstance() }
    val currentYear = remember { cal.get(Calendar.YEAR) }
    val currentMonth = remember { cal.get(Calendar.MONTH) + 1 }
    val currentMonthPrefix = remember { String.format(Locale.US, "%04d-%02d", currentYear, currentMonth) }
    val daysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    val allCompletedDates = remember(targetHabits) {
        targetHabits.flatMap { it.completedDates }
    }
    val distinctCompletedDates = remember(allCompletedDates) {
        allCompletedDates.toSet()
    }

    val activeDaysCount = if (selectedHabit != null) selectedHabit.completedDates.size else distinctCompletedDates.size
    val totalCompletions = allCompletedDates.size

    val bestStreak = if (selectedHabit != null) selectedHabit.streakDays else (habits.maxOfOrNull { it.streakDays } ?: 0)
    val currentStreak = if (selectedHabit != null) {
        if (selectedHabit.isCompletedToday) selectedHabit.streakDays else 0
    } else {
        targetHabits.map { if (it.isCompletedToday) it.streakDays else 0 }.maxOrNull() ?: 0
    }

    // Expected dates = number of calendar days from habit start date/creation to today (0 if created today)
    // Completed dates = actual days the habit was performed
    val totalExpectedDays = remember(targetHabits, selectedHabit) {
        if (selectedHabit != null) {
            calculateDaysFromStartToToday(selectedHabit.startDate)
        } else {
            targetHabits.maxOfOrNull { calculateDaysFromStartToToday(it.startDate) } ?: 0
        }
    }

    val totalCompletedDays = remember(targetHabits, selectedHabit) {
        if (selectedHabit != null) {
            selectedHabit.completedDates.size
        } else {
            distinctCompletedDates.size
        }
    }

    val expectedDatesCount = totalExpectedDays
    val completedDatesCount = totalCompletedDays
    val overallAchievementRatio = if (expectedDatesCount == 0) {
        if (completedDatesCount > 0) 1f else 1f
    } else {
        (completedDatesCount.toFloat() / expectedDatesCount).coerceIn(0f, 1f)
    }
    val overallAchievementPercent = (overallAchievementRatio * 100).toInt()

    val completedInMonth = targetHabits.sumOf { h -> h.completedDates.count { it.startsWith(currentMonthPrefix) } }

    val currentWeekDates = remember {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        val firstDay = c.firstDayOfWeek
        var diff = dayOfWeek - firstDay
        if (diff < 0) diff += 7
        c.add(Calendar.DAY_OF_MONTH, -diff)

        val set = mutableSetOf<String>()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (i in 0 until 7) {
            set.add(fmt.format(c.time))
            c.add(Calendar.DAY_OF_MONTH, 1)
        }
        set
    }

    val weekCompletions = targetHabits.sumOf { h -> h.completedDates.count { it in currentWeekDates } }
    val monthCompletions = completedInMonth
    val yearCompletions = targetHabits.sumOf { h -> h.completedDates.count { it.startsWith("$currentYear-") } }
    val allTimeCompletions = totalCompletions

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Title
        item {
            Text(
                text = "Analysis",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
            )
        }

        // Gamification Card: "🏝 My island"
        item {
            IslandGamificationCard(
                totalCompletions = totalCompletions,
                habitName = selectedHabit?.name,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Filter Pills
        item {
            HabitFiltersRow(
                habits = habits,
                selected = if (isAllSelected) allLabel else selectedFilter,
                accentColor = accentColor,
                onSelect = { selectedFilter = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Activity Heatmap Card
        item {
            ActivityHeatmapCard(
                completedDates = distinctCompletedDates,
                activeDaysCount = activeDaysCount,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2x2 Stats Grid
        item {
            Stats2x2Grid(
                completions = totalCompletions,
                bestStreak = bestStreak,
                currentStreak = currentStreak,
                achievementPercent = overallAchievementPercent,
                completedInMonth = completedDatesCount,
                expectedInMonth = expectedDatesCount,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Achieved vs Expected Dates Card
        item {
            AchievedVsExpectedCard(
                achievedDates = completedDatesCount,
                expectedDates = expectedDatesCount,
                achievementRatio = overallAchievementRatio,
                achievementPercent = overallAchievementPercent,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Times Completed Breakdown
        item {
            TimesCompletedCard(
                thisWeek = weekCompletions,
                thisMonth = monthCompletions,
                thisYear = yearCompletions,
                allTime = allTimeCompletions
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Perfect Day Streak Banner
        item {
            PerfectDayStreakBanner(
                currentStreak = currentStreak,
                habitName = selectedHabit?.name
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Monthly Completions Bar Chart
        item {
            MonthlyCompletionsChart(
                targetHabits = targetHabits,
                currentYear = currentYear,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Consistency Donut Card
        item {
            ConsistencyDonutCard(
                targetHabits = targetHabits,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // By Day Of Week Chart
        item {
            DayOfWeekChart(
                targetHabits = targetHabits,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // By Habit Achievement List
        item {
            HabitAchievementCard(habits = if (selectedHabit != null) listOf(selectedHabit) else habits)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Summary 2x2
        item {
            SummaryMetricsCard(
                activeDays = activeDaysCount,
                perWeek = if (weekCompletions > 0) String.format(Locale.US, "%.1f", weekCompletions.toFloat()) else "0.0",
                perfectDays = currentStreak,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun IslandGamificationCard(
    totalCompletions: Int,
    habitName: String?,
    accentColor: Color
) {
    val level = (totalCompletions / 5) + 1
    val targetLevelCompletions = level * 5
    val remaining = maxOf(1, targetLevelCompletions - totalCompletions)
    val progress = (totalCompletions.toFloat() / targetLevelCompletions).coerceIn(0f, 1f)
    val titleText = if (habitName != null) "🏝 $habitName Island" else "🏝 My island"

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    shape = PillShape,
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "LEVEL $level",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$totalCompletions completed · $remaining more check-ins to reach Level ${level + 1}",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(PillShape),
                color = accentColor,
                trackColor = Color(0xFF2B231C)
            )
        }
    }
}

@Composable
private fun HabitFiltersRow(
    habits: List<Habit>,
    selected: String,
    accentColor: Color,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val allLabel = "All Habits (${habits.size})"
        val isAllSelected = selected == allLabel
        Surface(
            shape = PillShape,
            color = if (isAllSelected) accentColor else Color(0xFF161310),
            modifier = Modifier
                .clip(PillShape)
                .clickable { onSelect(allLabel) }
                .border(1.dp, if (isAllSelected) accentColor else Color(0xFF2B231C), PillShape)
        ) {
            Text(
                text = allLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAllSelected) Color.White else Color(0xFF9CA3AF),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        habits.forEach { habit ->
            val habitColor = Color(android.graphics.Color.parseColor(habit.colorHex))
            val isSelected = selected == habit.name
            Surface(
                shape = PillShape,
                color = if (isSelected) habitColor else Color(0xFF161310),
                modifier = Modifier
                    .clip(PillShape)
                    .clickable { onSelect(habit.name) }
                    .border(1.dp, if (isSelected) habitColor else Color(0xFF2B231C), PillShape)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else habitColor)
                    )
                    Text(
                        text = habit.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityHeatmapCard(
    completedDates: Set<String>,
    activeDaysCount: Int,
    accentColor: Color
) {
    // Generate 70 days (5 rows x 14 cols)
    val cells = remember(completedDates) {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        // start 69 days ago
        cal.add(Calendar.DAY_OF_YEAR, -69)
        val list = mutableListOf<Pair<String, Boolean>>()
        for (i in 0 until 70) {
            val dStr = fmt.format(cal.time)
            list.add(Pair(dStr, completedDates.contains(dStr)))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Heatmap",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$activeDaysCount Active Days",
                    fontSize = 13.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 rows x 14 columns grid of cells
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 14) {
                            val index = r * 14 + c
                            val isDone = if (index < cells.size) cells[index].second else false
                            val cellColor = if (isDone) accentColor else Color(0xFF1F1A15)
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stats2x2Grid(
    completions: Int,
    bestStreak: Int,
    currentStreak: Int,
    achievementPercent: Int,
    completedInMonth: Int,
    expectedInMonth: Int,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                modifier = Modifier.weight(1f),
                title = "COMPLETIONS",
                value = "$completions",
                accent = Color.White
            )
            StatBox(
                modifier = Modifier.weight(1f),
                title = "BEST STREAK",
                value = "$bestStreak days",
                accent = AccentAmber
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                modifier = Modifier.weight(1f),
                title = "CURRENT STREAK",
                value = "$currentStreak days",
                accent = accentColor
            )
            StatBox(
                modifier = Modifier.weight(1f),
                title = "ACHIEVEMENT",
                value = "$achievementPercent%",
                sub = "$completedInMonth of $expectedInMonth dates",
                accent = Color.White
            )
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    sub: String? = null,
    accent: Color
) {
    Card(
        shape = InnerCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
        modifier = modifier.border(1.dp, Color(0xFF2E2E3C), InnerCardShape)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            if (sub != null) {
                Text(
                    text = sub,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun AchievedVsExpectedCard(
    achievedDates: Int,
    expectedDates: Int,
    achievementRatio: Float,
    achievementPercent: Int,
    accentColor: Color
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2E2E3C), CardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Achieved vs. Expected Dates",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$achievedDates DATES COMPLETED",
                    fontSize = 12.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$expectedDates DATES EXPECTED",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            CircularProgressRing(
                progress = achievementRatio,
                size = 64.dp,
                strokeWidth = 6.dp,
                trackColor = Color(0xFF282836),
                gradientColors = listOf(accentColor, AccentAmber),
                centerText = "$achievementPercent%"
            )
        }
    }
}

@Composable
private fun TimesCompletedCard(
    thisWeek: Int,
    thisMonth: Int,
    thisYear: Int,
    allTime: Int
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C24)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2E2E3C), CardShape)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Times completed",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("This week", "$thisWeek")
                MetricColumn("This month", "$thisMonth")
                MetricColumn("This year", "$thisYear")
                MetricColumn("All time", "$allTime")
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF6B7280))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun PerfectDayStreakBanner(
    currentStreak: Int,
    habitName: String?
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1712)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF3B271A), CardShape)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AccentAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Fire",
                    tint = AccentAmber,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (habitName != null) "$habitName STREAK" else "CURRENT STREAK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentAmber,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$currentStreak days in a row. Don't break it!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MonthlyCompletionsChart(
    targetHabits: List<Habit>,
    currentYear: Int,
    accentColor: Color
) {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val values = months.mapIndexed { idx, _ ->
        val prefix = String.format(Locale.US, "%04d-%02d", currentYear, idx + 1)
        targetHabits.sumOf { h -> h.completedDates.count { it.startsWith(prefix) } }
    }
    val maxVal = maxOf(1, values.maxOrNull() ?: 1)

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Completions / Month",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                months.forEachIndexed { i, m ->
                    val v = values[i]
                    val fraction = if (v > 0) (v.toFloat() / maxVal.toFloat()).coerceIn(0.1f, 1f) else 0.05f
                    val isPeak = v == maxVal && v > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((80 * fraction).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isPeak) accentColor else if (v > 0) accentColor.copy(alpha = 0.6f) else Color(0xFF221A14))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = m,
                            fontSize = 10.sp,
                            color = if (isPeak) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsistencyDonutCard(
    targetHabits: List<Habit>,
    accentColor: Color
) {
    // Consistency over the last 90 days
    val count90 = remember(targetHabits) {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        cal.add(Calendar.DAY_OF_YEAR, -90)
        val cutoff = fmt.format(cal.time)
        targetHabits.sumOf { h -> h.completedDates.count { it >= cutoff } }
    }
    val expected90 = maxOf(1, 90 * targetHabits.size)
    val ratio = (count90.toFloat() / expected90).coerceIn(0f, 1f)
    val percent = (ratio * 100).toInt()

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "CONSISTENCY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$percent%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Last 90 days ($count90 completed)",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            CircularProgressRing(
                progress = ratio,
                size = 72.dp,
                strokeWidth = 8.dp,
                trackColor = Color(0xFF282018),
                gradientColors = listOf(accentColor, AccentAmber),
                centerText = "$percent%"
            )
        }
    }
}

@Composable
private fun DayOfWeekChart(
    targetHabits: List<Habit>,
    accentColor: Color
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    val counts = remember(targetHabits) {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val c = IntArray(7) { 0 }
        targetHabits.forEach { h ->
            h.completedDates.forEach { dStr ->
                try {
                    val d = fmt.parse(dStr)
                    if (d != null) {
                        cal.time = d
                        // Calendar.MONDAY is 2, SUNDAY is 1
                        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                        val idx = when (dayOfWeek) {
                            Calendar.MONDAY -> 0
                            Calendar.TUESDAY -> 1
                            Calendar.WEDNESDAY -> 2
                            Calendar.THURSDAY -> 3
                            Calendar.FRIDAY -> 4
                            Calendar.SATURDAY -> 5
                            Calendar.SUNDAY -> 6
                            else -> 0
                        }
                        c[idx]++
                    }
                } catch (_: Exception) {}
            }
        }
        c.toList()
    }

    val maxCount = maxOf(1, counts.maxOrNull() ?: 1)

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "WHEN ARE YOU MOST CONSISTENT?",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                letterSpacing = 1.sp
            )
            Text(
                text = "By day of week",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { idx, day ->
                    val count = counts[idx]
                    val fraction = if (count > 0) (count.toFloat() / maxCount.toFloat()).coerceIn(0.15f, 1f) else 0.08f
                    val isBest = count == maxCount && count > 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((60 * fraction).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isBest) AccentAmber else if (count > 0) accentColor.copy(alpha = 0.7f) else Color(0xFF221A14))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                            color = if (isBest) AccentAmber else Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitAchievementCard(habits: List<Habit>) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "By habit achievement",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            habits.forEach { habit ->
                val habitColor = Color(android.graphics.Color.parseColor(habit.colorHex))
                val rate = if (habit.totalDaysCompleted > 0) minOf(100, habit.totalDaysCompleted * 5) else if (habit.completedDates.isNotEmpty()) minOf(100, habit.completedDates.size * 5) else 10

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = habit.name,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$rate%",
                            fontSize = 13.sp,
                            color = habitColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { rate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(PillShape),
                        color = habitColor,
                        trackColor = Color(0xFF24242C)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricsCard(
    activeDays: Int,
    perWeek: String,
    perfectDays: Int,
    accentColor: Color
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, Color(0xFF2B231C), CardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Active days", fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(text = "$activeDays", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Per week", fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(text = perWeek, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Current streak", fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(text = "$perfectDays days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Focus", fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(text = "Consistent", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
        }
    }
}

private fun calculateDaysFromStartToToday(startDateStr: String): Int {
    return try {
        val calToday = Calendar.getInstance()
        val calStart = Calendar.getInstance()

        val currentYear = calToday.get(Calendar.YEAR)
        var parsed = false

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("MMM d, yyyy", Locale.US),
            SimpleDateFormat("MMM d", Locale.US),
            SimpleDateFormat("MMMM d", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US)
        )

        val cleanStr = startDateStr.trim()
        for (fmt in formats) {
            try {
                val d = fmt.parse(cleanStr)
                if (d != null) {
                    calStart.time = d
                    if (!cleanStr.contains(currentYear.toString()) && !fmt.toPattern().contains("yyyy")) {
                        calStart.set(Calendar.YEAR, currentYear)
                    }
                    parsed = true
                    break
                }
            } catch (_: Exception) {}
        }

        if (parsed) {
            calToday.set(Calendar.HOUR_OF_DAY, 0)
            calToday.set(Calendar.MINUTE, 0)
            calToday.set(Calendar.SECOND, 0)
            calToday.set(Calendar.MILLISECOND, 0)

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)
            calStart.set(Calendar.MILLISECOND, 0)

            val diffMillis = calToday.timeInMillis - calStart.timeInMillis
            val diffDays = (diffMillis / (1000L * 60 * 60 * 24)).toInt()
            maxOf(0, diffDays)
        } else {
            0
        }
    } catch (_: Exception) {
        0
    }
}

