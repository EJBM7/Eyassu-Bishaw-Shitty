package com.example.data

import android.content.Context
import com.example.model.AppSettings
import com.example.model.Habit
import com.example.model.HabitType
import com.example.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HabitRepository {
    private val todayDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val persistenceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var appContext: Context? = null

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _sections = MutableStateFlow(listOf("Habits", "Skill", "Others"))
    val sections: StateFlow<List<String>> = _sections.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _selectedHabitForDetail = MutableStateFlow<Habit?>(null)
    val selectedHabitForDetail: StateFlow<Habit?> = _selectedHabitForDetail.asStateFlow()

    fun initializePersistence(context: Context) {
        if (appContext != null) return
        val ctx = context.applicationContext
        appContext = ctx

        if (!LocalPersistence.isInitialized(ctx)) {
            val initialHabits = createInitialHabits()
            val initialTasks = createInitialTasks()
            val initialSettings = AppSettings()
            val initialSections = listOf("Habits", "Skill", "Others")

            _habits.value = initialHabits
            _tasks.value = initialTasks
            _settings.value = initialSettings
            _sections.value = initialSections

            LocalPersistence.saveHabits(ctx, initialHabits)
            LocalPersistence.saveTasks(ctx, initialTasks)
            LocalPersistence.saveSettings(ctx, initialSettings)
            LocalPersistence.saveSections(ctx, initialSections)
            LocalPersistence.markInitialized(ctx)
        } else {
            LocalPersistence.loadHabits(ctx)?.let { _habits.value = it }
            LocalPersistence.loadTasks(ctx)?.let { _tasks.value = it }
            LocalPersistence.loadSettings(ctx)?.let { _settings.value = it }
            LocalPersistence.loadSections(ctx)?.let { _sections.value = it }
        }

        // Setup background auto-persist collectors
        persistenceScope.launch {
            _habits.collect { list ->
                appContext?.let { LocalPersistence.saveHabits(it, list) }
            }
        }
        persistenceScope.launch {
            _tasks.collect { list ->
                appContext?.let { LocalPersistence.saveTasks(it, list) }
            }
        }
        persistenceScope.launch {
            _settings.collect { settings ->
                appContext?.let { LocalPersistence.saveSettings(it, settings) }
            }
        }
        persistenceScope.launch {
            _sections.collect { sections ->
                appContext?.let { LocalPersistence.saveSections(it, sections) }
            }
        }
    }

    fun selectHabitForDetail(habit: Habit?) {
        _selectedHabitForDetail.value = habit
    }

    fun addSection(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !_sections.value.contains(trimmed)) {
            _sections.update { it + trimmed }
        }
    }

    fun toggleDateCompletion(habitId: String, dateStr: String) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) {
                    val wasChecked = habit.completedDates.contains(dateStr)
                    val newCompletedDates = if (wasChecked) {
                        habit.completedDates - dateStr
                    } else {
                        habit.completedDates + dateStr
                    }
                    val isToday = dateStr == todayDateStr
                    val newIsCompletedToday = if (isToday) !wasChecked else habit.isCompletedToday
                    val newCurrentCount = if (isToday) {
                        if (!wasChecked) habit.targetCount else 0
                    } else habit.currentCountToday

                    val newTotal = newCompletedDates.size
                    val newStreak = if (newCompletedDates.contains(todayDateStr)) {
                        maxOf(1, newTotal)
                    } else {
                        maxOf(0, habit.streakDays)
                    }

                    habit.copy(
                        completedDates = newCompletedDates,
                        isCompletedToday = newIsCompletedToday,
                        currentCountToday = newCurrentCount,
                        totalDaysCompleted = newTotal,
                        streakDays = newStreak
                    )
                } else habit
            }
        }
        // Sync selected habit for detail
        _selectedHabitForDetail.value?.let { current ->
            if (current.id == habitId) {
                _selectedHabitForDetail.value = _habits.value.find { it.id == habitId }
            }
        }
    }

    fun handleHabitAction(habitId: String) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) {
                    if (habit.targetCount > 1) {
                        if (habit.currentCountToday >= habit.targetCount) {
                            // Already completed -> reset back to 0
                            val newCompletedDates = habit.completedDates - todayDateStr
                            habit.copy(
                                currentCountToday = 0,
                                isCompletedToday = false,
                                completedDates = newCompletedDates,
                                streakDays = maxOf(0, habit.streakDays - 1),
                                totalDaysCompleted = newCompletedDates.size
                            )
                        } else {
                            val step = if (habit.recordAmount > 0) habit.recordAmount else 1
                            val nextCount = habit.currentCountToday + step
                            val isDone = nextCount >= habit.targetCount
                            val newCompletedDates = if (isDone) habit.completedDates + todayDateStr else habit.completedDates
                            val newStreak = if (isDone && !habit.isCompletedToday) habit.streakDays + 1 else habit.streakDays
                            val newTotal = if (isDone && !habit.isCompletedToday) habit.totalDaysCompleted + 1 else habit.totalDaysCompleted
                            habit.copy(
                                currentCountToday = nextCount,
                                isCompletedToday = isDone,
                                completedDates = newCompletedDates,
                                streakDays = newStreak,
                                totalDaysCompleted = newTotal
                            )
                        }
                    } else {
                        val newCompleted = !habit.isCompletedToday
                        val newCompletedDates = if (newCompleted) {
                            habit.completedDates + todayDateStr
                        } else {
                            habit.completedDates - todayDateStr
                        }
                        val newCount = if (newCompleted) 1 else 0
                        val newStreak = if (newCompleted) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
                        val newTotal = if (newCompleted) habit.totalDaysCompleted + 1 else maxOf(0, habit.totalDaysCompleted - 1)

                        habit.copy(
                            isCompletedToday = newCompleted,
                            currentCountToday = newCount,
                            completedDates = newCompletedDates,
                            streakDays = newStreak,
                            totalDaysCompleted = newTotal
                        )
                    }
                } else habit
            }
        }
        _selectedHabitForDetail.value?.let { current ->
            if (current.id == habitId) {
                _selectedHabitForDetail.value = _habits.value.find { it.id == habitId }
            }
        }
    }

    fun toggleHabitCompletion(habitId: String) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) {
                    val newCompleted = !habit.isCompletedToday
                    val newCompletedDates = if (newCompleted) {
                        habit.completedDates + todayDateStr
                    } else {
                        habit.completedDates - todayDateStr
                    }
                    val newCount = if (newCompleted) habit.targetCount else 0
                    val newStreak = if (newCompleted) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
                    val newTotal = if (newCompleted) habit.totalDaysCompleted + 1 else maxOf(0, habit.totalDaysCompleted - 1)

                    habit.copy(
                        isCompletedToday = newCompleted,
                        currentCountToday = newCount,
                        completedDates = newCompletedDates,
                        streakDays = newStreak,
                        totalDaysCompleted = newTotal
                    )
                } else habit
            }
        }
        // Update detail if open
        _selectedHabitForDetail.value?.let { current ->
            if (current.id == habitId) {
                _selectedHabitForDetail.value = _habits.value.find { it.id == habitId }
            }
        }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun incrementHabitCount(habitId: String) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) {
                    val step = if (habit.recordAmount > 0) habit.recordAmount else 1
                    val nextCount = habit.currentCountToday + step
                    val isDone = nextCount >= habit.targetCount
                    val newCompletedDates = if (isDone) habit.completedDates + todayDateStr else habit.completedDates
                    val newStreak = if (isDone && !habit.isCompletedToday) habit.streakDays + 1 else habit.streakDays
                    val newTotal = if (isDone && !habit.isCompletedToday) habit.totalDaysCompleted + 1 else habit.totalDaysCompleted
                    habit.copy(
                        currentCountToday = nextCount,
                        isCompletedToday = isDone,
                        completedDates = newCompletedDates,
                        streakDays = newStreak,
                        totalDaysCompleted = newTotal
                    )
                } else habit
            }
        }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun addHabit(newHabit: Habit) {
        _habits.update { it + newHabit }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun updateHabit(updated: Habit) {
        _habits.update { list -> list.map { if (it.id == updated.id) updated else it } }
        if (_selectedHabitForDetail.value?.id == updated.id) {
            _selectedHabitForDetail.value = updated
        }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun deleteHabit(habitId: String) {
        _habits.update { list -> list.filterNot { it.id == habitId } }
        if (_selectedHabitForDetail.value?.id == habitId) {
            _selectedHabitForDetail.value = null
        }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun addHabitNote(habitId: String, noteText: String, dateStr: String = todayDateStr) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) {
                    val updatedNotes = habit.notes + (dateStr to noteText)
                    habit.copy(notes = updatedNotes)
                } else habit
            }
        }
        if (_selectedHabitForDetail.value?.id == habitId) {
            _selectedHabitForDetail.value = _habits.value.find { it.id == habitId }
        }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun resetHabitToday(habitId: String) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) {
                    val newCompletedDates = habit.completedDates - todayDateStr
                    habit.copy(
                        isCompletedToday = false,
                        currentCountToday = 0,
                        completedDates = newCompletedDates,
                        streakDays = maxOf(0, habit.streakDays - 1),
                        totalDaysCompleted = newCompletedDates.size
                    )
                } else habit
            }
        }
        if (_selectedHabitForDetail.value?.id == habitId) {
            _selectedHabitForDetail.value = _habits.value.find { it.id == habitId }
        }
        appContext?.let { LocalPersistence.saveHabits(it, _habits.value) }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
        }
        appContext?.let { LocalPersistence.saveTasks(it, _tasks.value) }
    }

    fun addTask(newTask: Task) {
        _tasks.update { listOf(newTask) + it }
        appContext?.let { LocalPersistence.saveTasks(it, _tasks.value) }
    }

    fun updateTask(updated: Task) {
        _tasks.update { list -> list.map { if (it.id == updated.id) updated else it } }
        appContext?.let { LocalPersistence.saveTasks(it, _tasks.value) }
    }

    fun deleteTask(taskId: String) {
        _tasks.update { list -> list.filterNot { it.id == taskId } }
        appContext?.let { LocalPersistence.saveTasks(it, _tasks.value) }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
        appContext?.let { LocalPersistence.saveSettings(it, _settings.value) }
    }

    private val _archivedHabits = MutableStateFlow<List<Habit>>(emptyList())
    val archivedHabits: StateFlow<List<Habit>> = _archivedHabits.asStateFlow()

    fun archiveHabit(habitId: String) {
        val toArchive = _habits.value.find { it.id == habitId } ?: return
        _habits.update { it.filterNot { h -> h.id == habitId } }
        _archivedHabits.update { it + toArchive }
        if (_selectedHabitForDetail.value?.id == habitId) {
            _selectedHabitForDetail.value = null
        }
    }

    fun restoreArchivedHabit(habitId: String) {
        val toRestore = _archivedHabits.value.find { it.id == habitId } ?: return
        _archivedHabits.update { it.filterNot { h -> h.id == habitId } }
        _habits.update { it + toRestore }
    }

    fun permanentlyDeleteArchivedHabit(habitId: String) {
        _archivedHabits.update { it.filterNot { h -> h.id == habitId } }
    }

    fun clearAllProgress() {
        _habits.update { list ->
            list.map { habit ->
                habit.copy(
                    currentCountToday = 0,
                    isCompletedToday = false,
                    streakDays = 0,
                    totalDaysCompleted = 0,
                    completedDates = emptySet(),
                    notes = emptyMap()
                )
            }
        }
    }

    fun eraseAllData() {
        _habits.value = emptyList()
        _tasks.value = emptyList()
        _archivedHabits.value = emptyList()
        _sections.value = listOf("Habits")
        _settings.value = AppSettings()
    }

    fun exportBackupJson(): String {
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val habitsArray = org.json.JSONArray()
        for (habit in _habits.value) {
            val obj = org.json.JSONObject()
            obj.put("id", habit.id)
            obj.put("name", habit.name)
            obj.put("description", habit.description)
            obj.put("category", habit.category)
            obj.put("colorHex", habit.colorHex)
            obj.put("iconName", habit.iconName)
            obj.put("targetCount", habit.targetCount)
            obj.put("unit", habit.unit)
            obj.put("startDate", habit.startDate)
            obj.put("frequency", habit.frequency)
            obj.put("timeOfDay", habit.timeOfDay)
            obj.put("quote", habit.quote)
            obj.put("streakDays", habit.streakDays)
            obj.put("totalDaysCompleted", habit.totalDaysCompleted)

            val datesArr = org.json.JSONArray()
            for (date in habit.completedDates) {
                datesArr.put(date)
            }
            obj.put("completedDates", datesArr)

            val notesObj = org.json.JSONObject()
            for ((k, v) in habit.notes) {
                notesObj.put(k, v)
            }
            obj.put("notes", notesObj)

            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        val tasksArray = org.json.JSONArray()
        for (task in _tasks.value) {
            val tobj = org.json.JSONObject()
            tobj.put("id", task.id)
            tobj.put("title", task.title)
            tobj.put("description", task.description)
            tobj.put("dueTime", task.dueTime)
            tobj.put("dueDate", task.dueDate)
            tobj.put("category", task.category)
            tobj.put("isCompleted", task.isCompleted)
            tobj.put("priority", task.priority)
            tasksArray.put(tobj)
        }
        root.put("tasks", tasksArray)

        val sectionsArray = org.json.JSONArray()
        for (s in _sections.value) {
            sectionsArray.put(s)
        }
        root.put("sections", sectionsArray)

        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = org.json.JSONObject(jsonString)
            if (root.has("habits")) {
                val habitsArr = root.getJSONArray("habits")
                val restored = mutableListOf<Habit>()
                for (i in 0 until habitsArr.length()) {
                    val hObj = habitsArr.getJSONObject(i)
                    val completedDatesSet = mutableSetOf<String>()
                    if (hObj.has("completedDates")) {
                        val dArr = hObj.getJSONArray("completedDates")
                        for (j in 0 until dArr.length()) {
                            completedDatesSet.add(dArr.getString(j))
                        }
                    }
                    val notesMap = mutableMapOf<String, String>()
                    if (hObj.has("notes")) {
                        val nObj = hObj.getJSONObject("notes")
                        val keys = nObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            notesMap[k] = nObj.getString(k)
                        }
                    }

                    restored.add(
                        Habit(
                            id = hObj.optString("id", UUID.randomUUID().toString()),
                            name = hObj.optString("name", "Restored Habit"),
                            description = hObj.optString("description", ""),
                            category = hObj.optString("category", "Habits"),
                            colorHex = hObj.optString("colorHex", "#FF6D00"),
                            iconName = hObj.optString("iconName", "check"),
                            targetCount = hObj.optInt("targetCount", 1),
                            unit = hObj.optString("unit", "Count"),
                            startDate = hObj.optString("startDate", "Sep 20"),
                            frequency = hObj.optString("frequency", "Daily"),
                            timeOfDay = hObj.optString("timeOfDay", "Anytime"),
                            quote = hObj.optString("quote", "Show up for yourself."),
                            streakDays = hObj.optInt("streakDays", 0),
                            totalDaysCompleted = hObj.optInt("totalDaysCompleted", completedDatesSet.size),
                            completedDates = completedDatesSet,
                            notes = notesMap,
                            isCompletedToday = completedDatesSet.contains(todayDateStr)
                        )
                    )
                }
                if (restored.isNotEmpty()) {
                    _habits.value = restored
                }
            }

            if (root.has("tasks")) {
                val tasksArr = root.getJSONArray("tasks")
                val restoredTasks = mutableListOf<Task>()
                for (i in 0 until tasksArr.length()) {
                    val tObj = tasksArr.getJSONObject(i)
                    restoredTasks.add(
                        Task(
                            id = tObj.optString("id", UUID.randomUUID().toString()),
                            title = tObj.optString("title", "Task"),
                            description = tObj.optString("description", ""),
                            dueTime = tObj.optString("dueTime", "11:00 PM"),
                            dueDate = tObj.optString("dueDate", "Today"),
                            category = tObj.optString("category", "Personal"),
                            isCompleted = tObj.optBoolean("isCompleted", false),
                            priority = tObj.optInt("priority", 1)
                        )
                    )
                }
                if (restoredTasks.isNotEmpty()) {
                    _tasks.value = restoredTasks
                }
            }

            if (root.has("sections")) {
                val secArr = root.getJSONArray("sections")
                val restoredSec = mutableListOf<String>()
                for (i in 0 until secArr.length()) {
                    restoredSec.add(secArr.getString(i))
                }
                if (restoredSec.isNotEmpty()) {
                    _sections.value = restoredSec
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importFromExternalApp(appType: String, content: String): Int {
        var count = 0
        try {
            when {
                appType.contains("TickTick", ignoreCase = true) -> {
                    // TickTick CSV or JSON format
                    val lines = content.lines()
                    val newHabits = mutableListOf<Habit>()
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.isEmpty() || trimmed.startsWith("Folder Name") || trimmed.startsWith("Title") || trimmed.startsWith("#")) continue
                        val parts = trimmed.split(",")
                        val title = parts.getOrNull(0)?.replace("\"", "")?.trim() ?: continue
                        if (title.isNotBlank()) {
                            newHabits.add(
                                Habit(
                                    id = UUID.randomUUID().toString(),
                                    name = title,
                                    category = "TickTick",
                                    colorHex = "#3B82F6",
                                    iconName = "check",
                                    startDate = "Sep 20",
                                    quote = "Imported from TickTick"
                                )
                            )
                            count++
                        }
                    }
                    if (newHabits.isNotEmpty()) {
                        _habits.update { it + newHabits }
                    }
                }
                appType.contains("Loop", ignoreCase = true) -> {
                    // Loop Habit Tracker CSV: Position, Name, Description, Question, Color, Frequency...
                    val lines = content.lines()
                    val newHabits = mutableListOf<Habit>()
                    for ((idx, line) in lines.withIndex()) {
                        if (idx == 0 && line.contains("Position", ignoreCase = true)) continue
                        val parts = line.split(",")
                        if (parts.size >= 2) {
                            val name = parts.getOrNull(1)?.replace("\"", "")?.trim() ?: parts.getOrNull(0)?.replace("\"", "")?.trim() ?: ""
                            if (name.isNotBlank()) {
                                newHabits.add(
                                    Habit(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        category = "Loop",
                                        colorHex = "#10B981",
                                        iconName = "fitness",
                                        startDate = "Sep 20",
                                        quote = "Imported from Loop"
                                    )
                                )
                                count++
                            }
                        }
                    }
                    if (newHabits.isNotEmpty()) {
                        _habits.update { it + newHabits }
                    }
                }
                else -> {
                    // HabitKit JSON or general JSON list
                    val root = org.json.JSONObject(content)
                    val habitsArr = root.optJSONArray("habits") ?: root.optJSONArray("items")
                    if (habitsArr != null) {
                        val newHabits = mutableListOf<Habit>()
                        for (i in 0 until habitsArr.length()) {
                            val h = habitsArr.getJSONObject(i)
                            val name = h.optString("name", h.optString("title", ""))
                            if (name.isNotBlank()) {
                                newHabits.add(
                                    Habit(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        category = h.optString("category", "Habits"),
                                        colorHex = h.optString("color", "#F59E0B"),
                                        iconName = "book",
                                        startDate = "Sep 20"
                                    )
                                )
                                count++
                            }
                        }
                        if (newHabits.isNotEmpty()) {
                            _habits.update { it + newHabits }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    fun resetToSampleData() {
        _habits.value = createInitialHabits()
        _tasks.value = createInitialTasks()
        _settings.value = AppSettings()
    }

    companion object {
        val instance = HabitRepository()

        private fun createInitialHabits(): List<Habit> {
            val prayingSepDates = (1..16).map { day ->
                val dayStr = if (day < 10) "0$day" else "$day"
                "2026-09-$dayStr"
            }.toSet() + setOf("2026-08-30", "2026-08-31")

            return listOf(
                Habit(
                    id = "habit_2",
                    name = "Praying",
                    description = "Morning and evening spiritual reflection",
                    category = "Mindfulness",
                    colorHex = "#0A84FF",
                    iconName = "self_improvement",
                    habitType = HabitType.NORMAL,
                    unit = "count",
                    targetCount = 1,
                    currentCountToday = 1,
                    isCompletedToday = true,
                    streakDays = 18,
                    totalDaysCompleted = 18,
                    frequency = "Daily",
                    timeOfDay = "Morning",
                    quote = "Discipline equals freedom.",
                    completedDates = prayingSepDates
                ),
                Habit(
                    id = "habit_1",
                    name = "ግማሽ or ሩብ ሰዓሊ ለ1 በቀን",
                    description = "Daily learning session for continuous progress",
                    category = "Learning",
                    colorHex = "#EC4899",
                    iconName = "book",
                    habitType = HabitType.NORMAL,
                    unit = "pages",
                    targetCount = 2,
                    currentCountToday = 0,
                    isCompletedToday = false,
                    streakDays = 0,
                    totalDaysCompleted = 4,
                    frequency = "Daily",
                    timeOfDay = "Anytime",
                    quote = "Consistency beats intensity.",
                    completedDates = setOf("2026-09-14", "2026-09-15", "2026-09-16", "2026-09-17", "2026-09-18")
                ),
                Habit(
                    id = "habit_3",
                    name = "Read 15 Pages a day",
                    description = "Read physical books for knowledge and wisdom",
                    category = "Mindfulness",
                    colorHex = "#8B5CF6",
                    iconName = "menu_book",
                    habitType = HabitType.NORMAL,
                    unit = "page",
                    targetCount = 15,
                    currentCountToday = 0,
                    isCompletedToday = false,
                    streakDays = 0,
                    totalDaysCompleted = 7,
                    frequency = "Daily",
                    timeOfDay = "Evening",
                    quote = "A reader lives a thousand lives.",
                    completedDates = setOf("2026-09-02", "2026-09-05", "2026-09-08", "2026-09-12", "2026-09-15", "2026-09-16")
                )
            )
        }

        private fun createInitialTasks(): List<Task> {
            return listOf(
                Task(
                    id = "task_1",
                    title = "Explore app settings and filters",
                    description = "Filter tasks by category or search through your list.",
                    dueTime = "09:00 AM",
                    dueDate = "Today",
                    category = "Ideas",
                    isCompleted = false,
                    priority = 1 // Low
                ),
                Task(
                    id = "task_2",
                    title = "Welcome to Simple Tasks! 🍊",
                    description = "Tap the circle on the left to mark this task complete.",
                    dueTime = "10:00 AM",
                    dueDate = "Today",
                    category = "Personal",
                    isCompleted = false,
                    priority = 1 // Low
                ),
                Task(
                    id = "task_3",
                    title = "Organize today's goals",
                    description = "Prioritize work and personal items using color-coded badges.",
                    dueTime = "02:00 PM",
                    dueDate = "Today",
                    category = "Work",
                    isCompleted = false,
                    priority = 3 // High
                ),
                Task(
                    id = "task_4",
                    title = "Buy fresh groceries & oranges",
                    description = "Pick up healthy produce from the local market.",
                    dueTime = "06:00 PM",
                    dueDate = "Today",
                    category = "Shopping",
                    isCompleted = false,
                    priority = 2 // Medium
                )
            )
        }
    }
}
