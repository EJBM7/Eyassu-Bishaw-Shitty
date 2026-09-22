package com.example.ui.screens.tasks

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Task
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.CategoryBadgeBg
import com.example.ui.theme.CategoryBadgeText
import com.example.ui.theme.PriorityHighBg
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowBg
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumBg
import com.example.ui.theme.PriorityMediumColor

@Composable
fun TasksScreen(
    tasks: List<Task>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onNewTaskClick: () -> Unit,
    accentColor: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Done"
    var selectedCategoryFilter by remember { mutableStateOf("All") } // "All", "Personal", "Work", "Shopping", "Ideas"
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    val filteredTasks = tasks.filter { task ->
        val matchesStatus = when (selectedStatusFilter) {
            "Pending" -> !task.isCompleted
            "Done" -> task.isCompleted
            else -> true
        }
        val matchesCategory = when (selectedCategoryFilter) {
            "All" -> true
            else -> task.category.equals(selectedCategoryFilter, ignoreCase = true)
        }
        val matchesSearch = searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true)

        matchesStatus && matchesCategory && matchesSearch
    }

    val totalCount = tasks.size
    val completedCount = completedTasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressPercent = (progressFraction * 100).toInt()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Header matching Screenshot_20260920-044714_Brave.jpg
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Orange rounded icon with white checkmark
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentOrange,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Simple Tasks",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$completedCount of $totalCount completed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentOrange
                            )
                        }
                    }

                    // Search and Help icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E1A16),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { isSearchExpanded = !isSearchExpanded }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchExpanded) AccentOrange else Color(0xFF9E968F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E1A16),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = "Help",
                                    tint = Color(0xFF9E968F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar (expandable)
            if (isSearchExpanded) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF161310),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .border(1.dp, Color(0xFF2B231C), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color(0xFF756F68),
                                modifier = Modifier.size(18.dp)
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search tasks...", color = Color(0xFF756F68), fontSize = 14.sp) },
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
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Daily Progress Card matching Screenshot_20260920-044714_Brave.jpg
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221A13)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .border(1.dp, Color(0xFF35271C), RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Progress",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEDE8E0)
                            )

                            Text(
                                text = "$progressPercent%",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        // Progress Bar: Dark warm track with accent fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF33271D))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progressFraction.coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(accentColor)
                            )
                        }

                        Text(
                            text = "$completedCount of $totalCount tasks finished. You got this!",
                            fontSize = 13.sp,
                            color = Color(0xFF9E968F)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Status Filter Tabs Row: [All 4] [Pending 4] [Done 0] matching Screenshot_20260920-044714_Brave.jpg
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusTabPill(
                        label = "All",
                        count = tasks.size,
                        isSelected = selectedStatusFilter == "All",
                        accentColor = accentColor,
                        onClick = { selectedStatusFilter = "All" }
                    )
                    StatusTabPill(
                        label = "Pending",
                        count = pendingTasks.size,
                        isSelected = selectedStatusFilter == "Pending",
                        accentColor = accentColor,
                        onClick = { selectedStatusFilter = "Pending" }
                    )
                    StatusTabPill(
                        label = "Done",
                        count = completedTasks.size,
                        isSelected = selectedStatusFilter == "Done",
                        accentColor = accentColor,
                        onClick = { selectedStatusFilter = "Done" }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Category Filter Chips: [All] [Personal] [Work] [Shopping] [Ideas] matching Screenshot
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All", "Personal", "Work", "Shopping", "Ideas").forEach { cat ->
                        val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.18f) else Color(0xFF161310),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) accentColor else Color(0xFF2A221B),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) accentColor else Color(0xFFD6D1CA),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching tasks found" else "No tasks in this list",
                            color = Color(0xFF756F68),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        accentColor = accentColor,
                        onToggle = { onToggleTask(task.id) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Floating Action Button matching Screenshot_20260920-044714_Brave.jpg: "+ New Task"
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = accentColor,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onNewTaskClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New Task",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "New Task",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// Status Tab Pill [All 4], [Pending 4], [Done 0]
@Composable
private fun StatusTabPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) accentColor else Color(0xFF161310),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, if (isSelected) accentColor else Color(0xFF2B231C), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else Color(0xFFD6D1CA)
            )

            // Circular Count Badge
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF2C241C),
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$count",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF9E968F)
                    )
                }
            }
        }
    }
}

// Task Item Card matching Screenshot_20260920-044714_Brave.jpg
@Composable
private fun TaskCardItem(
    task: Task,
    accentColor: Color,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161310)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onEdit)
            .border(1.dp, Color(0xFF2B231C), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Large Circular Checkbox on the left matching Screenshot
            Surface(
                shape = CircleShape,
                color = if (task.isCompleted) accentColor else Color.Transparent,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (task.isCompleted) accentColor else Color(0xFF443A31),
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggle)
            ) {
                if (task.isCompleted) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Center details: Title, description, badges
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) Color(0xFF756F68) else Color.White,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = Color(0xFF9E968F),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Badges Row: [Category] [Priority]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge (Khaki / Warm Umber)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CategoryBadgeBg
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CategoryBadgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Priority Badge
                    val (pBg, pColor, pText) = when (task.priority) {
                        3 -> Triple(PriorityHighBg, PriorityHighColor, "High")
                        2 -> Triple(PriorityMediumBg, PriorityMediumColor, "Medium")
                        1 -> Triple(PriorityLowBg, PriorityLowColor, "Low")
                        else -> Triple(Color(0xFF221F1C), Color(0xFF9E968F), "None")
                    }

                    if (task.priority > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = pBg
                        ) {
                            Text(
                                text = pText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = pColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Right side: Trash can icon matching Screenshot
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFF6E665E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
