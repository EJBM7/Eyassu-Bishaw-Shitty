package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HabitIcon(
    iconName: String,
    tint: Color = Color.White,
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val trimmed = iconName.trim()
    val isEmoji = trimmed.any { Character.isSurrogate(it) || it.code > 127 || Character.getType(it) == Character.SURROGATE.toInt() } ||
            (trimmed.length in 1..6 && trimmed.none { it.isLetter() })

    if (isEmoji) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = trimmed,
                fontSize = (size.value * 0.85f).sp,
                lineHeight = (size.value * 0.9f).sp
            )
        }
    } else {
        val vector = when (trimmed.lowercase()) {
            "heart", "favorite", "love" -> Icons.Filled.Favorite
            "fitness", "gym", "fitness_center", "workout" -> Icons.Filled.FitnessCenter
            "run", "running", "directions_run" -> Icons.Filled.DirectionsRun
            "walk", "walking", "steps", "directions_walk" -> Icons.Filled.DirectionsWalk
            "water", "water_drop", "drink" -> Icons.Filled.WaterDrop
            "fire", "local_fire_department", "burn" -> Icons.Filled.LocalFireDepartment
            "mind", "meditation", "self_improvement", "zen" -> Icons.Filled.SelfImprovement
            "coffee", "tea", "local_cafe" -> Icons.Filled.LocalCafe
            "night", "sleep", "bedtime", "nightlight" -> Icons.Filled.Nightlight
            "art", "palette", "brush", "draw" -> Icons.Filled.Palette
            "work", "business" -> Icons.Filled.Work
            "star", "achievement" -> Icons.Filled.Star
            "book", "read", "menu_book" -> Icons.Filled.MenuBook
            "check", "check_circle", "done" -> Icons.Filled.Check
            "code", "computer", "laptop" -> Icons.Filled.Code
            "timer", "alarm", "clock" -> Icons.Filled.Timer
            "game", "sports_esports", "play" -> Icons.Filled.SportsEsports
            "music", "music_note", "song" -> Icons.Filled.MusicNote
            "money", "savings", "finance" -> Icons.Filled.Savings
            "food", "eat", "restaurant", "meal" -> Icons.Filled.Restaurant
            "sun", "sunny", "morning", "wb_sunny" -> Icons.Filled.WbSunny
            "brain", "psychology", "think" -> Icons.Filled.Psychology
            "clean", "cleaning_services" -> Icons.Filled.CleaningServices
            "bike", "cycling", "directions_bike" -> Icons.Filled.DirectionsBike
            "plant", "park", "nature" -> Icons.Filled.Park
            "bell", "notifications", "alarm_on" -> Icons.Filled.Notifications
            "note", "journal", "edit_note" -> Icons.Filled.EditNote
            "flag", "goal" -> Icons.Filled.Flag
            "spa", "health" -> Icons.Filled.Spa
            "swim", "pool" -> Icons.Filled.Pool
            "school", "study", "learn" -> Icons.Filled.School
            else -> Icons.Filled.CheckCircle
        }
        Icon(
            imageVector = vector,
            contentDescription = trimmed,
            tint = tint,
            modifier = modifier.size(size)
        )
    }
}
