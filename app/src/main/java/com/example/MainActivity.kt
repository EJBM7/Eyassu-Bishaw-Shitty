package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.HabitRepository
import com.example.model.AppSettings
import com.example.model.Habit
import com.example.model.MainTab
import com.example.model.Task
import com.example.ui.components.DotGridBackground
import com.example.ui.components.ExpandingFab
import com.example.ui.components.FloatingDockBar
import com.example.ui.modals.FocusSessionModal
import com.example.ui.modals.HabitDetailModal
import com.example.ui.modals.NewHabitModal
import com.example.ui.modals.NewTaskModal
import com.example.ui.modals.NotesModal
import com.example.ui.modals.TaskDatePickerModal
import com.example.ui.screens.analysis.AnalysisScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.lock.AppLockScreen
import com.example.ui.screens.settings.AppearanceScreen
import com.example.ui.screens.settings.BackupsScreen
import com.example.ui.screens.settings.PreferencesScreen
import com.example.ui.screens.settings.SecurityScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.today.TodayScreen
import com.example.ui.theme.HabitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HabitRepository.instance.initializePersistence(applicationContext)
        setContent {
            HabitApp()
        }
    }
}

@Composable
fun HabitApp() {
    val context = LocalContext.current
    val repository = remember {
        HabitRepository.instance.apply {
            initializePersistence(context.applicationContext)
        }
    }
    val habits by repository.habits.collectAsState()
    val sections by repository.sections.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val settings by repository.settings.collectAsState()
    val selectedHabitForDetail by repository.selectedHabitForDetail.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.TODAY) }
    var settingsSubScreen by remember { mutableStateOf<String?>(null) }
    var isAppLocked by remember { mutableStateOf(false) }

    // Automatically lock app when closed / sent to background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, settings.appLockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (settings.appLockEnabled) {
                    isAppLocked = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isFabExpanded by remember { mutableStateOf(false) }
    var showNewTaskModal by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<Task?>(null) }
    var showNewHabitModal by remember { mutableStateOf(false) }
    var selectedHabitForEdit by remember { mutableStateOf<Habit?>(null) }
    var showFocusModal by remember { mutableStateOf(false) }
    var showNotesModal by remember { mutableStateOf(false) }
    var showTaskDatePickerModal by remember { mutableStateOf(false) }
    var taskDateTimeLabel by remember { mutableStateOf("Today · 11:00 PM") }

    // Intercept back button to navigate back to Today tab or dismiss modals/sub-screens
    BackHandler(
        enabled = currentTab != MainTab.TODAY ||
                settingsSubScreen != null ||
                selectedHabitForDetail != null ||
                showNewTaskModal ||
                showNewHabitModal ||
                showFocusModal ||
                showNotesModal ||
                showTaskDatePickerModal ||
                isFabExpanded
    ) {
        when {
            selectedHabitForDetail != null -> repository.selectHabitForDetail(null)
            showNewTaskModal -> showNewTaskModal = false
            showNewHabitModal -> showNewHabitModal = false
            showFocusModal -> showFocusModal = false
            showNotesModal -> showNotesModal = false
            showTaskDatePickerModal -> showTaskDatePickerModal = false
            isFabExpanded -> isFabExpanded = false
            settingsSubScreen != null -> {
                settingsSubScreen = null
                currentTab = MainTab.TODAY
            }
            currentTab != MainTab.TODAY -> currentTab = MainTab.TODAY
        }
    }

    val pendingTasksCount = tasks.count { !it.isCompleted }
    val accentColor = remember(settings.accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(settings.accentColorHex))
        } catch (_: Exception) {
            Color(0xFF0A84FF)
        }
    }

    HabitTheme(
        darkTheme = settings.isDarkMode,
        accentColor = accentColor
    ) {
        DotGridBackground(
            pattern = settings.bgPattern,
            isDark = settings.isDarkMode
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main Content Screen
                Crossfade(
                    targetState = Pair(currentTab, settingsSubScreen),
                    label = "screen_crossfade"
                ) { (tab, subScreen) ->
                    when {
                        tab == MainTab.SETTINGS && subScreen == "Appearance" -> {
                            AppearanceScreen(
                                settings = settings,
                                onUpdateSettings = { repository.updateSettings(it) },
                                onBack = {
                                    settingsSubScreen = null
                                    currentTab = MainTab.TODAY
                                }
                            )
                        }
                        tab == MainTab.SETTINGS && subScreen == "Preferences" -> {
                            PreferencesScreen(
                                settings = settings,
                                onUpdateSettings = { repository.updateSettings(it) },
                                onBack = {
                                    settingsSubScreen = null
                                    currentTab = MainTab.TODAY
                                }
                            )
                        }
                        tab == MainTab.SETTINGS && subScreen == "Backups" -> {
                            BackupsScreen(
                                onResetSampleData = { repository.resetToSampleData() },
                                onBack = {
                                    settingsSubScreen = null
                                    currentTab = MainTab.TODAY
                                }
                            )
                        }
                        tab == MainTab.SETTINGS && subScreen == "Security" -> {
                            SecurityScreen(
                                settings = settings,
                                onUpdateSettings = { repository.updateSettings(it) },
                                onBack = {
                                    settingsSubScreen = null
                                    currentTab = MainTab.TODAY
                                }
                            )
                        }
                        tab == MainTab.TODAY -> {
                            TodayScreen(
                                habits = habits,
                                tasks = tasks,
                                settings = settings,
                                onToggleHabit = { repository.handleHabitAction(it) },
                                onIncrementHabit = { repository.incrementHabitCount(it) },
                                onToggleDate = { habitId, dateStr ->
                                    repository.toggleDateCompletion(habitId, dateStr)
                                },
                                onHabitClick = { repository.selectHabitForDetail(it) },
                                onToggleTask = { repository.toggleTaskCompletion(it) },
                                onFocusClick = { showFocusModal = true },
                                onNotesClick = { showNotesModal = true },
                                onViewAllTasks = { currentTab = MainTab.TASKS },
                                onNewHabitClick = { showNewHabitModal = true }
                            )
                        }
                        tab == MainTab.CALENDAR -> {
                            CalendarScreen(
                                habits = habits,
                                tasks = tasks,
                                onToggleHabitForDate = { habitId, dateStr ->
                                    repository.toggleDateCompletion(habitId, dateStr)
                                },
                                onHabitClick = { repository.selectHabitForDetail(it) },
                                onToggleTask = { repository.toggleTaskCompletion(it) },
                                onEditTask = { task ->
                                    selectedTaskForEdit = task
                                    showNewTaskModal = true
                                },
                                onNewHabitClick = { showNewHabitModal = true },
                                onNewTaskClick = {
                                    selectedTaskForEdit = null
                                    showNewTaskModal = true
                                }
                            )
                        }
                        tab == MainTab.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                accentColor = accentColor,
                                onToggleTask = { repository.toggleTaskCompletion(it) },
                                onDeleteTask = { repository.deleteTask(it) },
                                onEditTask = { task ->
                                    selectedTaskForEdit = task
                                    showNewTaskModal = true
                                },
                                onNewTaskClick = {
                                    selectedTaskForEdit = null
                                    showNewTaskModal = true
                                }
                            )
                        }
                        tab == MainTab.HABITS -> {
                            HabitsScreen(
                                habits = habits,
                                sections = sections,
                                accentColor = accentColor,
                                onAddSection = { repository.addSection(it) },
                                onToggleHabit = { repository.handleHabitAction(it) },
                                onIncrementHabit = { repository.incrementHabitCount(it) },
                                onHabitClick = { repository.selectHabitForDetail(it) },
                                onNewHabitClick = { showNewHabitModal = true }
                            )
                        }
                        tab == MainTab.ANALYSIS -> {
                            AnalysisScreen(habits = habits)
                        }
                        tab == MainTab.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                habitsCount = habits.size,
                                onUpdateSettings = { repository.updateSettings(it) },
                                onNavigateAppearance = { settingsSubScreen = "Appearance" },
                                onNavigatePreferences = { settingsSubScreen = "Preferences" },
                                onNavigateBackups = { settingsSubScreen = "Backups" },
                                onNavigateSecurity = { settingsSubScreen = "Security" }
                            )
                        }
                    }
                }

                // Floating Action Button (Only on Today screen; Tasks and Habits screens have their own dedicated single-action add options)
                if (settingsSubScreen == null && currentTab == MainTab.TODAY) {
                    ExpandingFab(
                        isExpanded = isFabExpanded,
                        onToggle = { isFabExpanded = !isFabExpanded },
                        accentColor = accentColor,
                        onNewTaskClick = {
                            selectedTaskForEdit = null
                            showNewTaskModal = true
                        },
                        onNewHabitClick = { showNewHabitModal = true },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                // Floating Bottom Dock Navigation
                if (settingsSubScreen == null) {
                    FloatingDockBar(
                        currentTab = currentTab,
                        pendingTaskCount = pendingTasksCount,
                        tabOrder = settings.tabOrder,
                        accentColor = accentColor,
                        onTabSelected = { tab ->
                            currentTab = tab
                            isFabExpanded = false
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                // Modals
                if (showNewTaskModal) {
                    NewTaskModal(
                        onDismiss = {
                            showNewTaskModal = false
                            selectedTaskForEdit = null
                        },
                        taskToEdit = selectedTaskForEdit,
                        onSaveTask = { repository.addTask(it) },
                        onUpdateTask = { repository.updateTask(it) },
                        onDeleteTask = { repository.deleteTask(it) },
                        onOpenDatePicker = { showTaskDatePickerModal = true },
                        selectedDateTimeLabel = taskDateTimeLabel
                    )
                }

                if (showTaskDatePickerModal) {
                    TaskDatePickerModal(
                        initialTime = "11:00 PM",
                        onDismiss = { showTaskDatePickerModal = false },
                        onConfirm = { dateStr, timeStr, _, _ ->
                            taskDateTimeLabel = "$dateStr · $timeStr"
                        }
                    )
                }

                if (showNewHabitModal) {
                    NewHabitModal(
                        habitToEdit = selectedHabitForEdit,
                        availableSections = sections,
                        onAddSection = { repository.addSection(it) },
                        onDismiss = {
                            showNewHabitModal = false
                            selectedHabitForEdit?.let { habit ->
                                repository.selectHabitForDetail(habit)
                            }
                            selectedHabitForEdit = null
                        },
                        onSaveHabit = { savedHabit ->
                            if (selectedHabitForEdit != null) {
                                repository.updateHabit(savedHabit)
                                repository.selectHabitForDetail(savedHabit)
                            } else {
                                repository.addHabit(savedHabit)
                            }
                            showNewHabitModal = false
                            selectedHabitForEdit = null
                        }
                    )
                }

                if (showFocusModal) {
                    FocusSessionModal(
                        habits = habits,
                        onDismiss = { showFocusModal = false },
                        onCompleteSession = { habitId ->
                            repository.incrementHabitCount(habitId)
                        }
                    )
                }

                if (showNotesModal) {
                    NotesModal(
                        onDismiss = { showNotesModal = false }
                    )
                }

                selectedHabitForDetail?.let { habit ->
                    HabitDetailModal(
                        habit = habit,
                        onDismiss = { repository.selectHabitForDetail(null) },
                        onToggleCompletion = { repository.toggleHabitCompletion(it) },
                        onToggleDate = { dateStr -> repository.toggleDateCompletion(habit.id, dateStr) },
                        onDeleteHabit = { repository.deleteHabit(it) },
                        onEditHabit = { habitToEdit ->
                            repository.selectHabitForDetail(null)
                            selectedHabitForEdit = habitToEdit
                            showNewHabitModal = true
                        },
                        onAddNote = { noteText ->
                            repository.addHabitNote(habit.id, noteText)
                        },
                        onResetToday = {
                            repository.resetHabitToday(habit.id)
                        }
                    )
                }

                // App Lock Screen Overlay when app lock is enabled and locked
                if (settings.appLockEnabled && isAppLocked) {
                    AppLockScreen(
                        onUnlocked = { isAppLocked = false },
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}
