package com.example.ui.screens.habits

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.model.Habit
import com.example.ui.components.HabitIcon
import com.example.ui.components.HabitProgressSquareButton
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.InnerCardShape
import com.example.ui.theme.PillShape

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    sections: List<String> = listOf("Habits", "Skill", "Others"),
    onAddSection: (String) -> Unit = {},
    onToggleHabit: (String) -> Unit,
    onIncrementHabit: (String) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onNewHabitClick: () -> Unit,
    accentColor: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("All Sections") }
    var showAddSectionDialog by remember { mutableStateOf(false) }

    val filteredHabits = habits.filter { habit ->
        val matchesSearch = searchQuery.isBlank() || habit.name.contains(searchQuery, ignoreCase = true)
        val matchesSection = selectedSection == "All Sections" || habit.category.equals(selectedSection, ignoreCase = true)
        matchesSearch && matchesSection
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habits",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Search Bar with Filter Icon
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = PillShape,
                        color = Color(0xFF161310),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFF2B231C), PillShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF756F68),
                                modifier = Modifier.size(18.dp)
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search habits...", color = Color(0xFF756F68), fontSize = 14.sp) },
                                singleLine = true,
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
                        }
                    }

                    // Filter Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161310))
                            .border(1.dp, Color(0xFF2B231C), CircleShape)
                            .clickable { /* quick filter */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Filter",
                            tint = Color(0xFF9E968F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section Filter Pills with + button to add custom section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HabitSectionPill(
                        title = "All Sections",
                        isSelected = selectedSection == "All Sections",
                        accentColor = accentColor,
                        onClick = { selectedSection = "All Sections" }
                    )

                    sections.forEach { sec ->
                        HabitSectionPill(
                            title = sec,
                            isSelected = selectedSection == sec,
                            accentColor = accentColor,
                            onClick = { selectedSection = sec }
                        )
                    }

                    // Add Custom Section Button Pill
                    Surface(
                        shape = PillShape,
                        color = Color(0xFF1D1712),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF352B21)),
                        modifier = Modifier
                            .clip(PillShape)
                            .clickable { showAddSectionDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Section",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "New",
                                color = accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section Header
            item {
                Text(
                    text = "HABITS (${filteredHabits.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF756F68),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Habit Items
            items(filteredHabits, key = { it.id }) { habit ->
                HabitListCard(
                    habit = habit,
                    onToggle = { onToggleHabit(habit.id) },
                    onIncrement = { onIncrementHabit(habit.id) },
                    onClick = { onHabitClick(habit) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Dedicated Habit Adding Option on Habits Page
        Surface(
            shape = CircleShape,
            color = accentColor,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
                .size(56.dp)
                .clip(CircleShape)
                .clickable(onClick = onNewHabitClick)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Habit",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    if (showAddSectionDialog) {
        var input by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddSectionDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1813),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332920)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Create Custom Section",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Section name", color = Color(0xFF6E665E), fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF262019),
                            unfocusedContainerColor = Color(0xFF262019),
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color(0xFF352B22),
                            cursorColor = accentColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddSectionDialog = false }) {
                            Text("Cancel", color = accentColor, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val trimmed = input.trim()
                                if (trimmed.isNotBlank()) {
                                    onAddSection(trimmed)
                                    selectedSection = trimmed
                                }
                                showAddSectionDialog = false
                            }
                        ) {
                            Text("Create", color = accentColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitSectionPill(
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color(0xFF161310),
        label = "pill_bg"
    )
    val textColor = if (isSelected) Color.White else Color(0xFF9E968F)

    Surface(
        shape = PillShape,
        color = bg,
        modifier = Modifier
            .clip(PillShape)
            .clickable(onClick = onClick)
            .border(1.dp, if (isSelected) accentColor else Color(0xFF2B231C), PillShape)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HabitListCard(
    habit: Habit,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onClick: () -> Unit
) {
    val habitColor = runCatching {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    }.getOrDefault(AccentOrange)
    val isComplete = habit.isCompletedToday

    Card(
        shape = InnerCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(InnerCardShape)
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF2B231C), InnerCardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Colored Icon Box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(habitColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, habitColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    HabitIcon(
                        iconName = habit.iconName,
                        tint = habitColor,
                        size = 22.dp
                    )
                }

                // Title and details
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = habit.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    val unitText = if (habit.unit.isNotBlank()) " ${habit.unit}" else ""
                    val progressSummary = "${habit.currentCountToday} / ${habit.targetCount}$unitText · ${habit.totalDaysCompleted} Total Days"
                    Text(
                        text = progressSummary,
                        fontSize = 12.sp,
                        color = Color(0xFF9E968F)
                    )
                }
            }

            val isMultiStep = habit.targetCount > 1
            val progress = if (habit.targetCount > 0) {
                (habit.currentCountToday.toFloat() / habit.targetCount).coerceIn(0f, 1f)
            } else 0f

            HabitProgressSquareButton(
                habitColor = habitColor,
                isComplete = isComplete,
                progress = progress,
                isMultiStep = isMultiStep,
                onClick = if (isMultiStep && !isComplete) onIncrement else onToggle,
                size = 44.dp,
                cornerRadius = 14.dp,
                testTag = "habits_screen_action_${habit.id}"
            )
        }
    }
}
