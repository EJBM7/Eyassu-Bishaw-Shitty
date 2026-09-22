package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentOrangeLight
import com.example.ui.theme.PillShape

@Composable
fun ExpandingFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNewTaskClick: () -> Unit,
    onNewHabitClick: () -> Unit,
    accentColor: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fab_rotation"
    )

    // Full screen overlay when expanded
    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
        )
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = 20.dp, bottom = 80.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Expanded Action 1: New Task
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it / 2 }
            ) {
                Surface(
                    shape = PillShape,
                    color = accentColor,
                    modifier = Modifier
                        .shadow(elevation = 8.dp, shape = PillShape)
                        .clip(PillShape)
                        .clickable {
                            onToggle()
                            onNewTaskClick()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "New Task",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "New Task",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Expanded Action 2: New Habit
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it / 2 }
            ) {
                Surface(
                    shape = PillShape,
                    color = accentColor,
                    modifier = Modifier
                        .shadow(elevation = 8.dp, shape = PillShape)
                        .clip(PillShape)
                        .clickable {
                            onToggle()
                            onNewHabitClick()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "New Habit",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "New Habit",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Main FAB Circle
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(58.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onToggle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isExpanded) {
                                Brush.linearGradient(listOf(Color(0xFF261F18), Color(0xFF1E1712)))
                            } else {
                                Brush.linearGradient(listOf(accentColor.copy(alpha = 0.85f), accentColor))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(if (isExpanded) 0f else rotation)
                    )
                }
            }
        }
    }
}
