package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MainTab
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange

@Composable
fun FloatingDockBar(
    currentTab: MainTab,
    pendingTaskCount: Int = 1,
    onTabSelected: (MainTab) -> Unit,
    tabOrder: List<String> = listOf("Today", "Calendar", "Task", "Habits", "Analysis", "Settings"),
    accentColor: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = barShape,
            color = Color(0xFF14110E).copy(alpha = 0.96f),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(elevation = 20.dp, shape = barShape, spotColor = Color.Black)
                .border(width = 1.dp, color = Color(0xFF2B221A), shape = barShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabOrder.forEach { rawTabName ->
                    val tab = rawTabName.trim()
                    when {
                        tab.equals("Today", ignoreCase = true) -> DockItem(
                            label = "Today",
                            isSelected = currentTab == MainTab.TODAY,
                            selectedIcon = Icons.Filled.CalendarToday,
                            unselectedIcon = Icons.Outlined.CalendarToday,
                            accentColor = accentColor,
                            onClick = { onTabSelected(MainTab.TODAY) }
                        )
                        tab.equals("Calendar", ignoreCase = true) -> DockItem(
                            label = "Calendar",
                            isSelected = currentTab == MainTab.CALENDAR,
                            selectedIcon = Icons.Filled.CalendarMonth,
                            unselectedIcon = Icons.Outlined.CalendarMonth,
                            accentColor = accentColor,
                            onClick = { onTabSelected(MainTab.CALENDAR) }
                        )
                        tab.equals("Task", ignoreCase = true) || tab.equals("Tasks", ignoreCase = true) -> DockItem(
                            label = "Task",
                            badgeCount = pendingTaskCount,
                            isSelected = currentTab == MainTab.TASKS,
                            selectedIcon = Icons.Filled.CheckCircle,
                            unselectedIcon = Icons.Outlined.CheckCircle,
                            accentColor = accentColor,
                            onClick = { onTabSelected(MainTab.TASKS) }
                        )
                        tab.equals("Habits", ignoreCase = true) || tab.equals("Habit", ignoreCase = true) -> DockItem(
                            label = "Habits",
                            isSelected = currentTab == MainTab.HABITS,
                            selectedIcon = Icons.Filled.FormatListBulleted,
                            unselectedIcon = Icons.Outlined.FormatListBulleted,
                            accentColor = accentColor,
                            onClick = { onTabSelected(MainTab.HABITS) }
                        )
                        tab.equals("Analysis", ignoreCase = true) || tab.equals("Analytics", ignoreCase = true) -> DockItem(
                            label = "Analysis",
                            isSelected = currentTab == MainTab.ANALYSIS,
                            selectedIcon = Icons.Filled.BarChart,
                            unselectedIcon = Icons.Outlined.BarChart,
                            accentColor = accentColor,
                            onClick = { onTabSelected(MainTab.ANALYSIS) }
                        )
                        tab.equals("Settings", ignoreCase = true) || tab.equals("Setting", ignoreCase = true) -> DockItem(
                            label = "Settings",
                            isSelected = currentTab == MainTab.SETTINGS,
                            selectedIcon = Icons.Filled.Settings,
                            unselectedIcon = Icons.Outlined.Settings,
                            accentColor = accentColor,
                            onClick = { onTabSelected(MainTab.SETTINGS) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DockItem(
    label: String,
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    accentColor: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animatedPillColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "pill_color"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color(0xFF9E968F),
        animationSpec = tween(220),
        label = "icon_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color(0xFF8A827B),
        animationSpec = tween(220),
        label = "text_color"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accentColor.copy(alpha = 0.3f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Icon container with active indicator pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(animatedPillColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-4).dp)
                            .size(16.dp)
                            .background(AccentAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
