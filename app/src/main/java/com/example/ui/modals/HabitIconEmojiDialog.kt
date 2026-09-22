package com.example.ui.modals

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.HabitIcon
import com.example.ui.theme.AccentOrange

@Composable
fun HabitIconEmojiDialog(
    currentIcon: String,
    accentColorHex: String,
    onDismiss: () -> Unit,
    onSelectIcon: (String) -> Unit
) {
    val activeColor = remember(accentColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(accentColorHex)) }
            .getOrDefault(AccentOrange)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var customEmojiInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Curated emoji catalog grouped by categories
    val emojisByCategory = remember {
        mapOf(
            "🏃 Fitness" to listOf(
                "🏃", "🏃‍♀️", "🚶", "🚶‍♀️", "🧘", "🧘‍♀️", "🏋️", "🏋️‍♀️", "🚴", "🚴‍♀️",
                "🏊", "🏊‍♀️", "🤸", "🤸‍♀️", "🥊", "🧗", "🧗‍♀️", "🛹", "⛷️", "⚽",
                "🏀", "🎾", "🏈", "🏸", "🏓", "🏌️", "🎯", "🤾", "🏇", "🚣"
            ),
            "💧 Health" to listOf(
                "💧", "🚰", "🍎", "🍏", "🥑", "🥗", "🥦", "🥕", "🍌", "🍇",
                "💊", "🩺", "🌙", "😴", "🛌", "🦷", "🧼", "☀️", "🍵", "🧊",
                "🫀", "🫁", "💆", "🚿", "🧂", "🍳", "🍓", "🍒", "🥝", "🍉"
            ),
            "📖 Mind" to listOf(
                "📖", "📚", "✍️", "💻", "🧠", "🎨", "🎵", "📝", "💼", "🔬",
                "🔭", "🎓", "🧩", "♟️", "🎸", "🎹", "🗣️", "📖", "💡", "📑",
                "📰", "🖌️", "📐", "🎻", "🎧", "🎙️", "🎭", "✒️", "📊", "🌐"
            ),
            "☕ Life" to listOf(
                "☕", "🍵", "🧹", "🌱", "🪴", "🐶", "🐱", "💰", "💳", "🛒",
                "🧺", "🚗", "✈️", "🏡", "🔑", "🍳", "🪴", "🌸", "🌻", "🌲",
                "🍽️", "⏰", "🧸", "🕯️", "🎁", "🪴", "🪞", "📮", "📦", "🛋️"
            ),
            "✨ Vibes" to listOf(
                "✨", "💖", "🔥", "⭐", "🌈", "🚀", "🏆", "🥇", "⚡", "🕊️",
                "🎉", "🎊", "🍀", "👑", "💎", "☀️", "🌊", "🪄", "🔮", "💫",
                "🧿", "🛡️", "🌺", "🌼", "💐", "🌟", "🎈", "❤️‍🔥", "💯", "🦾"
            )
        )
    }

    val allEmojis = remember { emojisByCategory.values.flatten().distinct() }

    val filteredEmojis = remember(selectedCategory, searchQuery) {
        val baseList = if (selectedCategory == "All") allEmojis else (emojisByCategory[selectedCategory] ?: allEmojis)
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter { it.contains(searchQuery.trim()) }
        }
    }

    val iconKeys = remember {
        listOf(
            "check" to "Checkmark",
            "run" to "Running",
            "walk" to "Walking",
            "fitness" to "Gym",
            "water" to "Water",
            "night" to "Sleep",
            "mind" to "Meditation",
            "coffee" to "Coffee",
            "food" to "Healthy Food",
            "fire" to "Calories/Fire",
            "star" to "Achievement",
            "heart" to "Love/Self Care",
            "work" to "Work",
            "code" to "Coding",
            "timer" to "Timer/Clock",
            "game" to "Gaming",
            "music" to "Music",
            "money" to "Savings",
            "sun" to "Morning/Sun",
            "brain" to "Brain/Focus",
            "clean" to "Cleaning",
            "bike" to "Cycling",
            "plant" to "Nature/Plant",
            "bell" to "Reminder/Bell",
            "note" to "Journal/Note",
            "flag" to "Goal/Flag",
            "spa" to "Spa/Wellness",
            "swim" to "Swimming",
            "school" to "Study/School",
            "art" to "Art/Drawing"
        )
    }

    val filteredIcons = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            iconKeys
        } else {
            iconKeys.filter {
                it.first.contains(searchQuery.trim(), ignoreCase = true) ||
                        it.second.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color(0xFF14110E),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Icon & Emoji",
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

                // Active Selected Preview Header Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1713)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .border(1.dp, Color(0xFF2C2219), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(activeColor.copy(alpha = 0.22f))
                                .border(2.dp, activeColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            HabitIcon(
                                iconName = currentIcon,
                                tint = activeColor,
                                size = 32.dp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Icon",
                                fontSize = 12.sp,
                                color = Color(0xFF9E968F),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select an emoji, icon, or enter custom text below",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1C1713),
                    contentColor = AccentOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AccentOrange,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "😃 Emoji",
                                fontSize = 13.5.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "🎨 Icons",
                                fontSize = 13.5.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "✍️ Custom",
                                fontSize = 13.5.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TAB 0: EMOJIS
                if (selectedTab == 0) {
                    // Category filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "🏃 Fitness", "💧 Health", "📖 Mind", "☕ Life", "✨ Vibes").forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) AccentOrange else Color(0xFF241D17),
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33271E)) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else Color(0xFFB0A79E),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 48.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredEmojis) { emoji ->
                            val isSelected = currentIcon == emoji
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0xFF221B15))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) activeColor else Color(0xFF32271D),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectIcon(emoji)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }

                // TAB 1: ICONS
                if (selectedTab == 1) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 72.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredIcons) { (key, label) ->
                            val isSelected = currentIcon.equals(key, ignoreCase = true)
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0xFF221B15))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) activeColor else Color(0xFF32271D),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectIcon(key)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                HabitIcon(
                                    iconName = key,
                                    tint = if (isSelected) activeColor else Color(0xFFD6D1CA),
                                    size = 28.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) activeColor else Color(0xFF9E968F),
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // TAB 2: CUSTOM EMOJI / TEXT INPUT
                if (selectedTab == 2) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Type Any Emoji or Character",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Open your keyboard's emoji panel and paste or type any emoji (e.g. 🚴, 🥊, 🥑, ♟️, 🎸)",
                            fontSize = 13.sp,
                            color = Color(0xFF9E968F)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = customEmojiInput,
                            onValueChange = { customEmojiInput = it },
                            placeholder = {
                                Text("Type or paste emoji here...", color = Color(0xFF6E665E), fontSize = 15.sp)
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E1813),
                                unfocusedContainerColor = Color(0xFF1E1813),
                                focusedIndicatorColor = AccentOrange,
                                unfocusedIndicatorColor = Color(0xFF33271E),
                                cursorColor = AccentOrange,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Live preview card of the custom emoji
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1713)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2C2219), RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Preview",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9E968F),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(activeColor.copy(alpha = 0.22f))
                                        .border(2.dp, activeColor, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val previewText = customEmojiInput.trim().ifEmpty { currentIcon }
                                    HabitIcon(
                                        iconName = previewText,
                                        tint = activeColor,
                                        size = 36.dp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick recommendations strip
                        Text(
                            text = "Popular Suggestions",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB0A79E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("💧", "🏃", "🧘", "📚", "🥑", "🎸", "🥊", "🎯", "⚡", "😴", "☕", "💰").forEach { rec ->
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF221B15),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33271E)),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable { customEmojiInput = rec }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = rec, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                val trimmed = customEmojiInput.trim()
                                if (trimmed.isNotBlank()) {
                                    onSelectIcon(trimmed)
                                    onDismiss()
                                }
                            },
                            enabled = customEmojiInput.trim().isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentOrange,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Use This Custom Emoji",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
