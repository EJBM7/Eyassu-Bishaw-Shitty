package com.example.ui.modals

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Habit
import com.example.model.HabitType
import com.example.ui.components.HabitIcon
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DarkBaseBackground
import com.example.ui.theme.DarkBorderStroke
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private enum class HabitModalStep {
    GALLERY,
    CUSTOM_FORM
}

data class HabitTemplateItem(
    val title: String,
    val iconName: String,
    val colorHex: String,
    val category: String, // "Health", "Exercise", "Life", "Mentality"
    val habitType: HabitType = HabitType.NORMAL,
    val targetCount: Int = 1,
    val unit: String = "Count",
    val quote: String = "Show up for yourself every day."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitModal(
    habitToEdit: Habit? = null,
    availableSections: List<String> = listOf("Habits", "Morning", "Afternoon", "Night", "Skill", "Others"),
    onAddSection: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveHabit: (Habit) -> Unit
) {
    var currentStep by remember(habitToEdit) {
        mutableStateOf(if (habitToEdit != null) HabitModalStep.CUSTOM_FORM else HabitModalStep.GALLERY)
    }

    // Form states (pre-populated if editing or pre-selected from template)
    var habitName by remember(habitToEdit) { mutableStateOf(habitToEdit?.name ?: "") }
    var habitDescription by remember(habitToEdit) {
        mutableStateOf(
            if (habitToEdit != null && habitToEdit.description.isNotBlank()) habitToEdit.description
            else (habitToEdit?.quote ?: "")
        )
    }
    var selectedCategory by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: (availableSections.firstOrNull() ?: "Habits")) }
    var selectedColorHex by remember(habitToEdit) { mutableStateOf(habitToEdit?.colorHex ?: "#FF6D00") }
    var selectedIconName by remember(habitToEdit) { mutableStateOf(habitToEdit?.iconName ?: "check") }

    // Habit Type & Goal
    var habitType by remember(habitToEdit) { mutableStateOf(habitToEdit?.habitType ?: HabitType.NORMAL) }
    var targetCount by remember(habitToEdit) { mutableIntStateOf(habitToEdit?.targetCount ?: 1) }
    var targetUnit by remember(habitToEdit) { mutableStateOf(habitToEdit?.unit ?: "Count") }
    var recordAmount by remember(habitToEdit) { mutableIntStateOf(habitToEdit?.recordAmount ?: 1) }
    var whenChecking by remember(habitToEdit) { mutableStateOf(habitToEdit?.whenChecking ?: "Auto") }

    // Frequency & Schedule
    var frequencyMode by remember(habitToEdit) { mutableStateOf(habitToEdit?.frequency ?: "Daily") }
    val selectedDaysOfWeek = remember(habitToEdit) {
        mutableStateListOf<Int>().apply {
            if (habitToEdit != null && habitToEdit.daysOfWeek.isNotEmpty()) {
                addAll(habitToEdit.daysOfWeek)
            } else {
                addAll(listOf(1, 2, 3, 4, 5, 6, 7))
            }
        }
    }
    var startDate by remember(habitToEdit) {
        val sdf = SimpleDateFormat("MMM dd", Locale.US)
        mutableStateOf(habitToEdit?.startDate ?: sdf.format(Date()))
    }
    var goalDays by remember(habitToEdit) { mutableStateOf(habitToEdit?.goalDays ?: "Forever") }

    // Reminders & Extras
    var reminderEnabled by remember(habitToEdit) { mutableStateOf(habitToEdit?.reminderTime != null) }
    var reminderTime by remember(habitToEdit) { mutableStateOf(habitToEdit?.reminderTime ?: "07:00") }
    var constantReminder by remember(habitToEdit) { mutableStateOf(habitToEdit?.constantReminder ?: false) }
    var autoPopUpLog by remember(habitToEdit) { mutableStateOf(habitToEdit?.autoPopUpLog ?: false) }
    var timeOfDay by remember(habitToEdit) { mutableStateOf(habitToEdit?.timeOfDay ?: "Anytime") }
    var weeklyTimesCount by remember(habitToEdit) { mutableIntStateOf(3) }
    var intervalDaysCount by remember(habitToEdit) { mutableIntStateOf(2) }
    val checklistSteps = remember(habitToEdit) {
        mutableStateListOf<String>().apply {
            if (habitToEdit != null && habitToEdit.checklistSteps.isNotEmpty()) {
                addAll(habitToEdit.checklistSteps)
            }
        }
    }

    // Dialog flags
    var showGoalDialog by remember { mutableStateOf(false) }
    var showNewSectionDialog by remember { mutableStateOf(false) }
    var showCustomUnitDialog by remember { mutableStateOf(false) }
    var showGoalDaysDialog by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }

    // Save action
    val performSave = {
        val finalName = if (habitName.isBlank()) "New Habit" else habitName.trim()
        val finalFrequency = when {
            frequencyMode.equals("Weekly", ignoreCase = true) -> "$weeklyTimesCount times / week"
            frequencyMode.equals("Interval", ignoreCase = true) -> "Every $intervalDaysCount days"
            else -> frequencyMode
        }
        val habit = if (habitToEdit != null) {
            habitToEdit.copy(
                name = finalName,
                description = habitDescription.trim(),
                category = selectedCategory,
                colorHex = selectedColorHex,
                iconName = selectedIconName,
                habitType = habitType,
                unit = targetUnit,
                targetCount = targetCount,
                recordAmount = recordAmount,
                whenChecking = whenChecking,
                startDate = startDate,
                goalDays = goalDays,
                constantReminder = constantReminder,
                autoPopUpLog = autoPopUpLog,
                frequency = finalFrequency,
                daysOfWeek = selectedDaysOfWeek.toSet(),
                timeOfDay = timeOfDay,
                checklistSteps = checklistSteps.filter { it.isNotBlank() },
                reminderTime = if (reminderEnabled) reminderTime else null,
                quote = if (habitDescription.isNotBlank()) habitDescription.trim() else habitToEdit.quote
            )
        } else {
            Habit(
                id = UUID.randomUUID().toString(),
                name = finalName,
                description = habitDescription.trim(),
                category = selectedCategory,
                colorHex = selectedColorHex,
                iconName = selectedIconName,
                habitType = habitType,
                unit = targetUnit,
                targetCount = targetCount,
                recordAmount = recordAmount,
                whenChecking = whenChecking,
                startDate = startDate,
                goalDays = goalDays,
                constantReminder = constantReminder,
                autoPopUpLog = autoPopUpLog,
                frequency = finalFrequency,
                daysOfWeek = selectedDaysOfWeek.toSet(),
                timeOfDay = timeOfDay,
                checklistSteps = checklistSteps.filter { it.isNotBlank() },
                reminderTime = if (reminderEnabled) reminderTime else null,
                quote = if (habitDescription.isNotBlank()) habitDescription.trim() else "Show up for yourself every day."
            )
        }
        onSaveHabit(habit)
        onDismiss()
    }

    // Templates library for the Habit Gallery
    val habitTemplates = remember {
        listOf(
            // Health
            HabitTemplateItem("Drink water", "💧", "#3B82F6", "Health", HabitType.AMOUNT, 8, "Cup", "Drink plenty of water every day to stay refreshed."),
            HabitTemplateItem("Early to bed", "🌙", "#8B5CF6", "Health", HabitType.NORMAL, 1, "Count", "Quality sleep gives you the energy to conquer tomorrow."),
            HabitTemplateItem("Take vitamins", "💊", "#EC4899", "Health", HabitType.NORMAL, 1, "Count", "Keep your immune system strong and steady."),
            HabitTemplateItem("Eat fruit & greens", "🍎", "#10B981", "Health", HabitType.NORMAL, 1, "Count", "Nourish your body with clean, fresh foods."),
            HabitTemplateItem("No sugar", "🚫", "#EF4444", "Health", HabitType.AVOID, 1, "Count", "Say no to processed sweets and excess sugary drinks."),
            HabitTemplateItem("Brush & floss", "✨", "#06B6D4", "Health", HabitType.NORMAL, 2, "Count", "Keep up consistent oral hygiene morning and night."),
            HabitTemplateItem("Skincare routine", "🧴", "#F59E0B", "Health", HabitType.NORMAL, 2, "Count", "Cleanse and moisturize for healthy, glowing skin."),

            // Exercise
            HabitTemplateItem("Running", "🏃", "#FF6D00", "Exercise", HabitType.AMOUNT, 30, "Minute", "A 30-minute run changes your entire mindset."),
            HabitTemplateItem("10,000 steps", "🚶", "#10B981", "Exercise", HabitType.AMOUNT, 10000, "Count", "Walk toward your fitness goals one step at a time."),
            HabitTemplateItem("Push-ups", "💪", "#EF4444", "Exercise", HabitType.AMOUNT, 30, "Count", "Strength is built rep by rep, day by day."),
            HabitTemplateItem("Morning stretch", "🧘", "#F59E0B", "Exercise", HabitType.AMOUNT, 15, "Minute", "Awaken your muscles and release accumulated tension."),
            HabitTemplateItem("Gym workout", "🏋️", "#8B5CF6", "Exercise", HabitType.AMOUNT, 45, "Minute", "Progress over perfection in every workout."),
            HabitTemplateItem("Cycling", "🚴", "#3B82F6", "Exercise", HabitType.AMOUNT, 20, "Minute", "Pedal your way to cardiovascular endurance."),

            // Life
            HabitTemplateItem("Read books", "📖", "#F59E0B", "Life", HabitType.AMOUNT, 20, "Page", "A reader lives a thousand lives before dying."),
            HabitTemplateItem("Journaling", "✍️", "#6366F1", "Life", HabitType.NORMAL, 1, "Count", "Write down thoughts to reflect and cultivate mindfulness."),
            HabitTemplateItem("Clean & organize", "🧹", "#14B8A6", "Life", HabitType.NORMAL, 1, "Count", "A clear desk and room leads to clear thinking."),
            HabitTemplateItem("Track expenses", "💰", "#10B981", "Life", HabitType.NORMAL, 1, "Count", "Know where every dollar goes to build financial freedom."),
            HabitTemplateItem("Learn coding / skill", "💻", "#3B82F6", "Life", HabitType.AMOUNT, 45, "Minute", "Sharpen your craft with daily deliberate practice."),
            HabitTemplateItem("Water plants", "🌱", "#10B981", "Life", HabitType.NORMAL, 1, "Count", "Nurture life around your home."),
            HabitTemplateItem("Call family", "📞", "#EC4899", "Life", HabitType.NORMAL, 1, "Count", "Stay closely connected with the people who matter most."),

            // Mentality
            HabitTemplateItem("Meditation", "🧘", "#10B981", "Mentality", HabitType.AMOUNT, 10, "Minute", "Quiet the chatter and cultivate inner stillness."),
            HabitTemplateItem("Deep breathing", "🌬️", "#06B6D4", "Mentality", HabitType.AMOUNT, 5, "Minute", "Reset your nervous system with mindful breathing."),
            HabitTemplateItem("Daily gratitude", "💖", "#EC4899", "Mentality", HabitType.NORMAL, 3, "Count", "Notice 3 good things each day to foster joy."),
            HabitTemplateItem("Limit screen time", "📵", "#EF4444", "Mentality", HabitType.AVOID, 1, "Count", "Live in the present moment, not behind a screen."),
            HabitTemplateItem("No social media", "🛑", "#F59E0B", "Mentality", HabitType.AVOID, 1, "Count", "Protect your focus from infinite scrolls and noise.")
        )
    }

    // Palette colors for habit
    val paletteColors = listOf(
        "#FF6D00", "#FF5722", "#F97316", "#F59E0B", "#FBBF24",
        "#EF4444", "#DC2626", "#F43F5E", "#EC4899", "#D946EF",
        "#8B5CF6", "#6366F1", "#3B82F6", "#0284C7", "#06B6D4",
        "#14B8A6", "#10B981", "#22C55E", "#84CC16", "#78716C"
    )

    val iconOptions = listOf(
        "💧", "🏃", "📖", "🧘", "🌙", "💪", "🍎", "💊",
        "✨", "☕", "🚴", "💰", "🧹", "🌱", "✍️", "💻",
        "check", "water_drop", "fitness_center", "menu_book",
        "self_improvement", "directions_run", "bedtime", "restaurant",
        "code", "star", "favorite", "local_fire_department"
    )

    // Full screen page presentation for authentic native experience
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBaseBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState == HabitModalStep.CUSTOM_FORM) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "HabitStepTransition"
            ) { step ->
                when (step) {
                    HabitModalStep.GALLERY -> {
                        // =========================================================
                        // SCREEN 1: HABIT GALLERY (Exact TickTick New Habit Page)
                        // =========================================================
                        var gallerySearchQuery by remember { mutableStateOf("") }
                        var selectedGalleryCategory by remember { mutableStateOf("All") }
                        val galleryCategories = listOf("All", "Health", "Exercise", "Life", "Mentality")

                        val filteredTemplates = remember(gallerySearchQuery, selectedGalleryCategory) {
                            habitTemplates.filter { template ->
                                val matchesCategory = selectedGalleryCategory == "All" || template.category.equals(selectedGalleryCategory, ignoreCase = true)
                                val matchesSearch = gallerySearchQuery.isBlank() ||
                                        template.title.contains(gallerySearchQuery, ignoreCase = true) ||
                                        template.category.contains(gallerySearchQuery, ignoreCase = true)
                                matchesCategory && matchesSearch
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBaseBackground)
                        ) {
                            // Top Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Text(
                                    text = "Habit Gallery",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                            }

                            // Search Bar
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkCardSurface,
                                border = BorderStroke(1.dp, DarkBorderStroke),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF9E968F),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    BasicTextField(
                                        value = gallerySearchQuery,
                                        onValueChange = { gallerySearchQuery = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp
                                        ),
                                        cursorBrush = SolidColor(AccentOrange),
                                        modifier = Modifier.weight(1f),
                                        decorationBox = { innerTextField ->
                                            if (gallerySearchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search habits or skills...",
                                                    color = Color(0xFF6E665E),
                                                    fontSize = 15.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )

                                    if (gallerySearchQuery.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = Color(0xFF9E968F),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { gallerySearchQuery = "" }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Main Scrollable List
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                // Top Action Card: "+ Create a Habit"
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, AccentOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                // Reset to clean custom habit
                                                habitName = ""
                                                habitDescription = ""
                                                selectedIconName = "check"
                                                selectedColorHex = "#FF6D00"
                                                habitType = HabitType.NORMAL
                                                targetCount = 1
                                                targetUnit = "Count"
                                                currentStep = HabitModalStep.CUSTOM_FORM
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .background(AccentOrange),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Create a Habit",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Create a Habit",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Customize name, icon, frequency, and goals",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF9E968F)
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = Color(0xFF6E665E),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Category Filter Pills
                                item {
                                    Column {
                                        Text(
                                            text = "CATEGORIES",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF9E968F),
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                        )

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(galleryCategories) { cat ->
                                                val isSelected = selectedGalleryCategory == cat
                                                Surface(
                                                    shape = PillShape,
                                                    color = if (isSelected) AccentOrange else DarkCardSurface,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) AccentOrange else DarkBorderStroke
                                                    ),
                                                    modifier = Modifier.clickable {
                                                        selectedGalleryCategory = cat
                                                    }
                                                ) {
                                                    Text(
                                                        text = cat,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else Color(0xFFD6D1CA),
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Habit Items in Category
                                items(filteredTemplates) { template ->
                                    val itemColor = runCatching {
                                        Color(android.graphics.Color.parseColor(template.colorHex))
                                    }.getOrDefault(AccentOrange)

                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(14.dp))
                                            .clickable {
                                                // Pre-fill fields and open custom form for fine-tuning
                                                habitName = template.title
                                                selectedIconName = template.iconName
                                                selectedColorHex = template.colorHex
                                                habitType = template.habitType
                                                targetCount = template.targetCount
                                                targetUnit = template.unit
                                                habitDescription = template.quote
                                                selectedCategory = template.category
                                                currentStep = HabitModalStep.CUSTOM_FORM
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            // Icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(itemColor.copy(alpha = 0.18f))
                                                    .border(1.dp, itemColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                HabitIcon(
                                                    iconName = template.iconName,
                                                    tint = itemColor,
                                                    size = 22.dp
                                                )
                                            }

                                            // Text info
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = template.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                val goalSummary = if (template.habitType == HabitType.AMOUNT) {
                                                    "Daily ${template.targetCount} ${template.unit}"
                                                } else if (template.habitType == HabitType.AVOID) {
                                                    "Avoid habit · Daily"
                                                } else {
                                                    "Daily goal · ${template.category}"
                                                }
                                                Text(
                                                    text = goalSummary,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF9E968F)
                                                )
                                            }

                                            // Quick One-Tap Add Button
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = AccentOrange.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.5f)),
                                                modifier = Modifier.clickable {
                                                    val newHabit = Habit(
                                                        id = UUID.randomUUID().toString(),
                                                        name = template.title,
                                                        description = template.quote,
                                                        category = template.category,
                                                        colorHex = template.colorHex,
                                                        iconName = template.iconName,
                                                        habitType = template.habitType,
                                                        unit = template.unit,
                                                        targetCount = template.targetCount,
                                                        recordAmount = 1,
                                                        whenChecking = "Auto",
                                                        startDate = startDate,
                                                        goalDays = "Forever",
                                                        constantReminder = false,
                                                        autoPopUpLog = false,
                                                        frequency = "Daily",
                                                        daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
                                                        quote = template.quote
                                                    )
                                                    onSaveHabit(newHabit)
                                                    onDismiss()
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add",
                                                        tint = AccentOrange,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = "Add",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AccentOrange
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HabitModalStep.CUSTOM_FORM -> {
                        // =========================================================
                        // SCREEN 2: CUSTOM HABIT CONFIGURATION (Matches Screenshots 1-6)
                        // =========================================================
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBaseBackground)
                        ) {
                            // Top App Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (habitToEdit != null) {
                                            onDismiss()
                                        } else {
                                            currentStep = HabitModalStep.GALLERY
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Text(
                                    text = if (habitToEdit != null) "Edit Habit" else "New Habit",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )

                                // Quick Save Checkmark in top bar for maximum efficiency
                                IconButton(onClick = performSave) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save",
                                        tint = AccentOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp)
                            ) {
                                // Habit Type Switcher (Positive / Avoid / Target Count)
                                item {
                                    val activeColor = runCatching {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    }.getOrDefault(AccentOrange)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E1712))
                                            .border(1.dp, Color(0xFF33271D), RoundedCornerShape(12.dp))
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf(
                                            Triple(HabitType.NORMAL, "Build Habit", Icons.Default.Check),
                                            Triple(HabitType.AVOID, "Avoid Habit", Icons.Default.Close),
                                            Triple(HabitType.AMOUNT, "Target Goal", Icons.Default.Timer)
                                        ).forEach { (type, label, icon) ->
                                            val isSelected = habitType == type
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(if (isSelected) activeColor else Color.Transparent)
                                                    .clickable { habitType = type },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else Color(0xFF9E968F),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else Color(0xFF9E968F)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Habit Name & Icon Input Card
                                item {
                                    val activeColor = runCatching {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    }.getOrDefault(AccentOrange)

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Icon / Emoji Preview Button with click to pick
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(activeColor.copy(alpha = 0.22f))
                                                        .border(2.dp, activeColor, RoundedCornerShape(14.dp))
                                                        .clickable { showIconPicker = true },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    HabitIcon(
                                                        iconName = selectedIconName,
                                                        tint = activeColor,
                                                        size = 28.dp
                                                    )
                                                    // Badge indicator in top-right
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .offset(x = (-3).dp, y = (-3).dp)
                                                            .size(15.dp)
                                                            .clip(CircleShape)
                                                            .background(activeColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Change Icon",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(9.dp)
                                                        )
                                                    }
                                                }

                                                // Name Input
                                                Column(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = habitName,
                                                        onValueChange = { habitName = it },
                                                        placeholder = {
                                                            Text(
                                                                "Habit name (e.g. Drink water)",
                                                                fontSize = 15.sp,
                                                                color = Color(0xFF6E665E)
                                                            )
                                                        },
                                                        singleLine = true,
                                                        colors = TextFieldDefaults.colors(
                                                            focusedContainerColor = Color.Transparent,
                                                            unfocusedContainerColor = Color.Transparent,
                                                            focusedIndicatorColor = activeColor,
                                                            unfocusedIndicatorColor = Color(0xFF332920),
                                                            cursorColor = activeColor,
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Direct Interactive Emoji & Icon Quick Picker Row
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val quickIcons = listOf("💧", "🏃", "📚", "🧘", "💪", "🥗", "🎯", "✨", "🔥", "☀️", "🎨", "✍️", "bed", "walk", "bike", "meditate")
                                                quickIcons.forEach { iconItem ->
                                                    val isSelected = selectedIconName == iconItem
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0xFF221B15),
                                                        border = BorderStroke(1.dp, if (isSelected) activeColor else Color(0xFF33271D)),
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clickable { selectedIconName = iconItem }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            if (iconItem.length <= 2) {
                                                                Text(text = iconItem, fontSize = 18.sp)
                                                            } else {
                                                                HabitIcon(
                                                                    iconName = iconItem,
                                                                    tint = if (isSelected) activeColor else Color(0xFFD6D1CA),
                                                                    size = 20.dp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // "+ More" Button to open 100+ Icons & Emojis dialog
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0xFF221B15),
                                                    border = BorderStroke(1.dp, activeColor.copy(alpha = 0.6f)),
                                                    modifier = Modifier
                                                        .height(40.dp)
                                                        .clickable { showIconPicker = true }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text("🎨", fontSize = 14.sp)
                                                        Text(
                                                            text = "+ More",
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = activeColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Habit Color & Custom Hex Card
                                item {
                                    val activeColor = runCatching {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    }.getOrDefault(AccentOrange)

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Habit Color",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF9E968F),
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Text(
                                                    text = "+ Custom Hex",
                                                    fontSize = 13.sp,
                                                    color = activeColor,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.clickable { showColorPicker = true }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // "+ Custom" Button as first pill
                                                Surface(
                                                    shape = RoundedCornerShape(18.dp),
                                                    color = Color(0xFF261F18),
                                                    border = BorderStroke(1.dp, Color(0xFF443528)),
                                                    modifier = Modifier
                                                        .height(36.dp)
                                                        .clickable { showColorPicker = true }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Palette,
                                                            contentDescription = null,
                                                            tint = activeColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = "Custom",
                                                            fontSize = 12.sp,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }

                                                paletteColors.forEach { hex ->
                                                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                                    val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(c)
                                                            .border(
                                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                                color = if (isSelected) Color.White else Color.Transparent,
                                                                shape = CircleShape
                                                            )
                                                            .clickable { selectedColorHex = hex },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Card 1: Frequency (Matches Screenshot 6)
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Frequency",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9E968F),
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Frequency Sub-tabs: DAILY, WEEKLY, INTERVAL
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                                            ) {
                                                listOf("DAILY", "WEEKLY", "INTERVAL").forEach { tab ->
                                                    val isSelected = frequencyMode.equals(tab, ignoreCase = true)
                                                    Column(
                                                        modifier = Modifier.clickable { frequencyMode = tab }
                                                    ) {
                                                        Text(
                                                            text = tab,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) AccentOrange else Color(0xFF9E968F)
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .width(42.dp)
                                                                .height(2.5.dp)
                                                                .background(if (isSelected) AccentOrange else Color.Transparent)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            if (frequencyMode.equals("DAILY", ignoreCase = true)) {
                                                // Quick Day Presets: Every day, Weekdays, Weekends
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf(
                                                        "Every day" to listOf(1, 2, 3, 4, 5, 6, 7),
                                                        "Weekdays" to listOf(2, 3, 4, 5, 6),
                                                        "Weekends" to listOf(1, 7)
                                                    ).forEach { (name, days) ->
                                                        val isSetMatch = selectedDaysOfWeek.toSet() == days.toSet()
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSetMatch) AccentOrange.copy(alpha = 0.2f) else Color(0xFF262019),
                                                            border = BorderStroke(1.dp, if (isSetMatch) AccentOrange else Color(0xFF352B22)),
                                                            modifier = Modifier.clickable {
                                                                selectedDaysOfWeek.clear()
                                                                selectedDaysOfWeek.addAll(days)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = name,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSetMatch) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSetMatch) AccentOrange else Color(0xFF9E968F),
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Day of Week Circles: S M T W T F S
                                                val daysLabels = listOf(
                                                    1 to "S", 2 to "M", 3 to "T", 4 to "W",
                                                    5 to "T", 6 to "F", 7 to "S"
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    daysLabels.forEach { (dayIndex, label) ->
                                                        val isDaySelected = selectedDaysOfWeek.contains(dayIndex)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(38.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isDaySelected) AccentOrange else Color(0xFF262019))
                                                                .border(
                                                                    1.dp,
                                                                    if (isDaySelected) AccentOrange else Color(0xFF352B22),
                                                                    CircleShape
                                                                )
                                                                .clickable {
                                                                    if (isDaySelected) {
                                                                        if (selectedDaysOfWeek.size > 1) {
                                                                            selectedDaysOfWeek.remove(dayIndex)
                                                                        }
                                                                    } else {
                                                                        selectedDaysOfWeek.add(dayIndex)
                                                                    }
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isDaySelected) Color.White else Color(0xFF756F68)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else if (frequencyMode.equals("WEEKLY", ignoreCase = true)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Target frequency",
                                                        fontSize = 14.sp,
                                                        color = Color(0xFFD6D1CA),
                                                        fontWeight = FontWeight.Medium
                                                    )

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF262019),
                                                            border = BorderStroke(1.dp, Color(0xFF352B22)),
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clickable { if (weeklyTimesCount > 1) weeklyTimesCount-- }
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text("-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        Text(
                                                            text = "$weeklyTimesCount times / week",
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AccentOrange
                                                        )

                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF262019),
                                                            border = BorderStroke(1.dp, Color(0xFF352B22)),
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clickable { if (weeklyTimesCount < 7) weeklyTimesCount++ }
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                // INTERVAL
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Repeat every",
                                                        fontSize = 14.sp,
                                                        color = Color(0xFFD6D1CA),
                                                        fontWeight = FontWeight.Medium
                                                    )

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF262019),
                                                            border = BorderStroke(1.dp, Color(0xFF352B22)),
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clickable { if (intervalDaysCount > 1) intervalDaysCount-- }
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text("-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        Text(
                                                            text = "Every $intervalDaysCount days",
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AccentOrange
                                                        )

                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF262019),
                                                            border = BorderStroke(1.dp, Color(0xFF352B22)),
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clickable { if (intervalDaysCount < 365) intervalDaysCount++ }
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Card 2: Goal, Start Date, Goal Days (Matches Screenshot 6)
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column {
                                            // Row 1: Goal -> Opens Goal Dialog (Screenshots 1-5)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showGoalDialog = true }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Goal",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val goalSummaryText = if (habitType == HabitType.AMOUNT) {
                                                        "Daily $targetCount $targetUnit"
                                                    } else if (habitType == HabitType.AVOID) {
                                                        "Avoid habit"
                                                    } else {
                                                        "Achieve it all"
                                                    }

                                                    Text(
                                                        text = goalSummaryText,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF9E968F)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = Color(0xFF6E665E),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFF262019))
                                            )

                                            // Row 2: Start Date (Calendar Popup Selector)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showStartDatePicker = true }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Start Date",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = startDate,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF9E968F)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = Color(0xFF6E665E),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFF262019))
                                            )

                                            // Row 3: Goal Days
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showGoalDaysDialog = true }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Goal Days",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Outlined.Info,
                                                        contentDescription = "Info",
                                                        tint = Color(0xFF6E665E),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = goalDays,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF9E968F)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = Color(0xFF6E665E),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Card: Time of Day
                                item {
                                    val activeColor = runCatching {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    }.getOrDefault(AccentOrange)

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Time of Day",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9E968F),
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf("Anytime", "Morning 🌅", "Afternoon ☀️", "Evening 🌇", "Night 🌙").forEach { tod ->
                                                    val isSelected = timeOfDay.equals(tod, ignoreCase = true)
                                                    Surface(
                                                        shape = PillShape,
                                                        color = if (isSelected) activeColor else Color(0xFF262019),
                                                        modifier = Modifier
                                                            .clip(PillShape)
                                                            .clickable { timeOfDay = tod }
                                                            .border(
                                                                1.dp,
                                                                if (isSelected) activeColor else Color(0xFF352B22),
                                                                PillShape
                                                            )
                                                    ) {
                                                        Text(
                                                            text = tod,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.White else Color(0xFFD6D1CA),
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Card 3: Section with + to create custom section (Matches Screenshot 6)
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Section",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF9E968F),
                                                    fontWeight = FontWeight.Medium
                                                )

                                                IconButton(
                                                    onClick = { showNewSectionDialog = true },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add Custom Section",
                                                        tint = Color(0xFFD6D1CA),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                availableSections.forEach { sec ->
                                                    val isSelected = selectedCategory.equals(sec, ignoreCase = true)
                                                    Surface(
                                                        shape = PillShape,
                                                        color = if (isSelected) AccentOrange else Color(0xFF262019),
                                                        modifier = Modifier
                                                            .clip(PillShape)
                                                            .clickable { selectedCategory = sec }
                                                            .border(
                                                                1.dp,
                                                                if (isSelected) AccentOrange else Color(0xFF352B22),
                                                                PillShape
                                                            )
                                                    ) {
                                                        Text(
                                                            text = sec,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.White else Color(0xFFD6D1CA),
                                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Card 4: Reminder (Matches Screenshot 6)
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Reminder",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9E968F),
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFF262019),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.dp, Color(0xFF352B22), RoundedCornerShape(8.dp))
                                                        .clickable { showTimePicker = true }
                                                ) {
                                                    Text(
                                                        text = reminderTime,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                                    )
                                                }

                                                Text(
                                                    text = "+ Add",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AccentOrange,
                                                    modifier = Modifier.clickable { showTimePicker = true }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFF262019))
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Constant Reminder 👑",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )

                                                Switch(
                                                    checked = constantReminder,
                                                    onCheckedChange = { constantReminder = it },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.White,
                                                        checkedTrackColor = AccentOrange,
                                                        uncheckedTrackColor = Color(0xFF262019)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Card 5: Auto pop-up of habit log
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Auto pop-up of habit log",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )

                                            Switch(
                                                checked = autoPopUpLog,
                                                onCheckedChange = { autoPopUpLog = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = AccentOrange,
                                                    uncheckedTrackColor = Color(0xFF262019)
                                                )
                                            )
                                        }
                                    }
                                }

                                // Card: Checklist Steps (Optional sub-habits/tasks)
                                item {
                                    var newStepInput by remember { mutableStateOf("") }
                                    val activeColor = runCatching {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    }.getOrDefault(AccentOrange)

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Checklist Steps (Optional)",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF9E968F),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (checklistSteps.isNotEmpty()) {
                                                    Text(
                                                        text = "${checklistSteps.size} steps",
                                                        fontSize = 12.sp,
                                                        color = activeColor,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Render existing steps
                                            checklistSteps.forEachIndexed { index, step ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = activeColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = step,
                                                            fontSize = 14.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { checklistSteps.removeAt(index) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove step",
                                                            tint = Color(0xFF756F68),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Add step input
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = newStepInput,
                                                    onValueChange = { newStepInput = it },
                                                    placeholder = {
                                                        Text("Add a sub-step...", fontSize = 13.sp, color = Color(0xFF6E665E))
                                                    },
                                                    singleLine = true,
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFF1E1812),
                                                        unfocusedContainerColor = Color(0xFF1E1812),
                                                        focusedIndicatorColor = activeColor,
                                                        unfocusedIndicatorColor = Color(0xFF33271D),
                                                        cursorColor = activeColor,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (newStepInput.isNotBlank()) activeColor else Color(0xFF262019),
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clickable {
                                                            if (newStepInput.isNotBlank()) {
                                                                checklistSteps.add(newStepInput.trim())
                                                                newStepInput = ""
                                                            }
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Add Step",
                                                            tint = if (newStepInput.isNotBlank()) Color.White else Color(0xFF756F68),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Motivational Quote & Slogan Card
                                item {
                                    val activeColor = runCatching {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    }.getOrDefault(AccentOrange)

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, DarkBorderStroke, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Motivational Slogan / Quote",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9E968F),
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = habitDescription,
                                                onValueChange = { habitDescription = it },
                                                placeholder = {
                                                    Text(
                                                        "e.g. Consistency is what transforms average into excellence.",
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF6E665E)
                                                    )
                                                },
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFF1E1812),
                                                    unfocusedContainerColor = Color(0xFF1E1812),
                                                    focusedIndicatorColor = activeColor,
                                                    unfocusedIndicatorColor = Color(0xFF33271D),
                                                    cursorColor = activeColor,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Quick quote inspiration chips
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf(
                                                    "Show up every day ✨",
                                                    "Small steps, big results 🚀",
                                                    "Consistency wins 🏆",
                                                    "One day or day one? ⏳",
                                                    "Discipline equals freedom 🎯"
                                                ).forEach { quoteText ->
                                                    Surface(
                                                        shape = PillShape,
                                                        color = Color(0xFF262019),
                                                        border = BorderStroke(1.dp, Color(0xFF352B22)),
                                                        modifier = Modifier
                                                            .clip(PillShape)
                                                            .clickable { habitDescription = quoteText }
                                                    ) {
                                                        Text(
                                                            text = quoteText,
                                                            fontSize = 11.5.sp,
                                                            color = Color(0xFFD6D1CA),
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom Sticky Save Button (Matches Screenshot 6)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Button(
                                    onClick = performSave,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentOrange,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = if (habitToEdit != null) "Save Changes" else "Save",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // GOAL DIALOG (Screenshots 1 - 5)
    // =========================================================================
    if (showGoalDialog) {
        GoalSettingDialog(
            currentHabitType = habitType,
            currentTargetCount = targetCount,
            currentTargetUnit = targetUnit,
            currentRecordAmount = recordAmount,
            currentWhenChecking = whenChecking,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newType, count, unit, recAmt, checking ->
                habitType = newType
                targetCount = count
                targetUnit = unit
                recordAmount = recAmt
                whenChecking = checking
                showGoalDialog = false
            },
            onRequestCustomUnit = {
                showCustomUnitDialog = true
            }
        )
    }

    // =========================================================================
    // ADD CUSTOM SECTION DIALOG
    // =========================================================================
    if (showNewSectionDialog) {
        var sectionInput by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewSectionDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1813),
                border = BorderStroke(1.dp, Color(0xFF332920)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "New Section",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = sectionInput,
                        onValueChange = { sectionInput = it },
                        placeholder = { Text("e.g. Morning, Health, Study", color = Color(0xFF6E665E), fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF262019),
                            unfocusedContainerColor = Color(0xFF262019),
                            focusedIndicatorColor = AccentOrange,
                            unfocusedIndicatorColor = Color(0xFF352B22),
                            cursorColor = AccentOrange,
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
                        TextButton(onClick = { showNewSectionDialog = false }) {
                            Text("Cancel", color = AccentOrange, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                val trimmed = sectionInput.trim()
                                if (trimmed.isNotBlank()) {
                                    onAddSection(trimmed)
                                    selectedCategory = trimmed
                                }
                                showNewSectionDialog = false
                            }
                        ) {
                            Text("Add", color = AccentOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // CUSTOM UNIT DIALOG
    // =========================================================================
    if (showCustomUnitDialog) {
        var customUnitInput by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCustomUnitDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1813),
                border = BorderStroke(1.dp, Color(0xFF332920)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Custom Unit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customUnitInput,
                        onValueChange = { customUnitInput = it },
                        placeholder = { Text("e.g. Gram, Glass, Lap", color = Color(0xFF6E665E), fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF262019),
                            unfocusedContainerColor = Color(0xFF262019),
                            focusedIndicatorColor = AccentOrange,
                            unfocusedIndicatorColor = Color(0xFF352B22),
                            cursorColor = AccentOrange,
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
                        TextButton(onClick = { showCustomUnitDialog = false }) {
                            Text("Cancel", color = AccentOrange, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                val trimmed = customUnitInput.trim()
                                if (trimmed.isNotBlank()) {
                                    targetUnit = trimmed
                                }
                                showCustomUnitDialog = false
                            }
                        ) {
                            Text("Confirm", color = AccentOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // GOAL DAYS DIALOG
    // =========================================================================
    if (showGoalDaysDialog) {
        val daysPresets = listOf("Forever", "7 Days", "21 Days", "30 Days", "66 Days", "100 Days", "365 Days")

        Dialog(onDismissRequest = { showGoalDaysDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1813),
                border = BorderStroke(1.dp, Color(0xFF332920)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Goal Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    daysPresets.forEach { preset ->
                        val isSelected = goalDays == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    goalDays = preset
                                    showGoalDaysDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preset,
                                fontSize = 15.sp,
                                color = if (isSelected) AccentOrange else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // TIME PRESET PICKER DIALOG
    // =========================================================================
    if (showTimePicker) {
        val timePresets = listOf("06:00 AM", "07:00 AM", "08:00 AM", "12:00 PM", "06:00 PM", "08:00 PM", "09:30 PM")

        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1813),
                border = BorderStroke(1.dp, Color(0xFF332920)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Set Reminder Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    timePresets.forEach { t ->
                        val isSelected = reminderTime == t
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    reminderTime = t
                                    reminderEnabled = true
                                    showTimePicker = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = t,
                                fontSize = 16.sp,
                                color = if (isSelected) AccentOrange else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // ICON & EMOJI PICKER DIALOG (100+ Curated Emojis, Material Icons, Custom Text)
    // =========================================================================
    if (showIconPicker) {
        HabitIconEmojiDialog(
            currentIcon = selectedIconName,
            accentColorHex = selectedColorHex,
            onDismiss = { showIconPicker = false },
            onSelectIcon = { selectedIconName = it }
        )
    }

    // =========================================================================
    // CUSTOM COLOR PICKER DIALOG (Palette Presets & Custom HEX Input)
    // =========================================================================
    if (showColorPicker) {
        CustomColorDialog(
            initialColorHex = selectedColorHex,
            currentIcon = selectedIconName,
            onDismiss = { showColorPicker = false },
            onColorSelected = { selectedColorHex = it }
        )
    }

    // =========================================================================
    // START DATE PICKER CALENDAR POPUP DIALOG
    // =========================================================================
    if (showStartDatePicker) {
        HabitDatePickerDialog(
            currentDateStr = startDate,
            accentColorHex = selectedColorHex,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { newDate ->
                startDate = newDate
                showStartDatePicker = false
            }
        )
    }
}

// =============================================================================
// GOAL SETTING DIALOG COMPONENT (Screenshots 1-5 Exact Design)
// =============================================================================
@Composable
private fun GoalSettingDialog(
    currentHabitType: HabitType,
    currentTargetCount: Int,
    currentTargetUnit: String,
    currentRecordAmount: Int,
    currentWhenChecking: String,
    onDismiss: () -> Unit,
    onConfirm: (HabitType, Int, String, Int, String) -> Unit,
    onRequestCustomUnit: () -> Unit
) {
    var selectedGoalMode by remember {
        mutableStateOf(if (currentHabitType == HabitType.AMOUNT) "AMOUNT" else "ACHIEVE_ALL")
    }
    var amountStr by remember { mutableStateOf(currentTargetCount.toString()) }
    var selectedUnit by remember { mutableStateOf(currentTargetUnit) }
    var whenCheckingStr by remember { mutableStateOf(currentWhenChecking) }
    var recordAmountStr by remember { mutableStateOf(currentRecordAmount.toString()) }

    var unitMenuExpanded by remember { mutableStateOf(false) }
    var checkingMenuExpanded by remember { mutableStateOf(false) }

    val unitOptions = listOf(
        "Count", "Cup", "Milliliter", "Minute", "Hour", "Kilometer", "Page", "Custom Unit"
    )

    val checkingOptions = listOf("Auto", "Manual", "Complete...")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1E1813), // Obsidian Warm Dark Container
            border = BorderStroke(1.dp, Color(0xFF332920)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                // Title
                Text(
                    text = "Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: Achieve it all
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGoalMode = "ACHIEVE_ALL" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGoalMode == "ACHIEVE_ALL",
                        onClick = { selectedGoalMode = "ACHIEVE_ALL" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentOrange,
                            unselectedColor = Color(0xFF756F68)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Achieve it all",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = if (selectedGoalMode == "ACHIEVE_ALL") FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Option 2: Reach a certain amount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGoalMode = "AMOUNT" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGoalMode == "AMOUNT",
                        onClick = { selectedGoalMode = "AMOUNT" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentOrange,
                            unselectedColor = Color(0xFF756F68)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reach a certain amount",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = if (selectedGoalMode == "AMOUNT") FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                // If Reach a certain amount is selected:
                if (selectedGoalMode == "AMOUNT") {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Row 1: Daily | [ 1 ] | [ Count ▾ ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily",
                                fontSize = 14.sp,
                                color = Color(0xFFD6D1CA),
                                modifier = Modifier.weight(1f)
                            )

                            // Amount Input Box
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF262019),
                                border = BorderStroke(1.dp, Color(0xFF352B22)),
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(38.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    BasicTextField(
                                        value = amountStr,
                                        onValueChange = {
                                            amountStr = it.filter { ch -> ch.isDigit() }.take(5)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(AccentOrange),
                                        singleLine = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Unit Dropdown Button
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF262019),
                                    border = BorderStroke(1.dp, Color(0xFF352B22)),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .clickable { unitMenuExpanded = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            text = selectedUnit,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = Color(0xFF9E968F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = unitMenuExpanded,
                                    onDismissRequest = { unitMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF221C16))
                                ) {
                                    unitOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = opt,
                                                    color = if (opt == "Custom Unit") AccentOrange else Color.White,
                                                    fontWeight = if (selectedUnit == opt) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                unitMenuExpanded = false
                                                if (opt == "Custom Unit") {
                                                    onRequestCustomUnit()
                                                } else {
                                                    selectedUnit = opt
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Row 2: When checking | [ Auto ▾ ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "When checking",
                                fontSize = 14.sp,
                                color = Color(0xFFD6D1CA),
                                modifier = Modifier.weight(1f)
                            )

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF262019),
                                    border = BorderStroke(1.dp, Color(0xFF352B22)),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .clickable { checkingMenuExpanded = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = whenCheckingStr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = Color(0xFF9E968F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = checkingMenuExpanded,
                                    onDismissRequest = { checkingMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF221C16))
                                ) {
                                    checkingOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = opt,
                                                    color = Color.White,
                                                    fontWeight = if (whenCheckingStr == opt) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                whenCheckingStr = opt
                                                checkingMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Row 3: Record ( Count ) | [ 1 ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Record ( $selectedUnit )",
                                fontSize = 14.sp,
                                color = Color(0xFFD6D1CA),
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF262019),
                                border = BorderStroke(1.dp, Color(0xFF352B22)),
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(38.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    BasicTextField(
                                        value = recordAmountStr,
                                        onValueChange = {
                                            recordAmountStr = it.filter { ch -> ch.isDigit() }.take(4)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(AccentOrange),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons: Cancel and OK in Orange
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = AccentOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TextButton(
                        onClick = {
                            val newHabitType = if (selectedGoalMode == "AMOUNT") HabitType.AMOUNT else HabitType.NORMAL
                            val parsedCount = amountStr.toIntOrNull() ?: 1
                            val parsedRec = recordAmountStr.toIntOrNull() ?: 1
                            onConfirm(
                                newHabitType,
                                if (parsedCount < 1) 1 else parsedCount,
                                selectedUnit,
                                if (parsedRec < 1) 1 else parsedRec,
                                whenCheckingStr
                            )
                        }
                    ) {
                        Text(
                            text = "OK",
                            color = AccentOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
