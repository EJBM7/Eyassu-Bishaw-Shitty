package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.Habit
import com.example.model.HabitType
import com.example.model.Task
import org.json.JSONArray
import org.json.JSONObject

object LocalPersistence {
    private const val PREFS_NAME = "habit_tracker_prefs"
    private const val KEY_INITIALIZED = "has_initialized_data_v2"
    private const val KEY_HABITS = "saved_habits"
    private const val KEY_TASKS = "saved_tasks"
    private const val KEY_SETTINGS = "saved_settings"
    private const val KEY_SECTIONS = "saved_sections"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isInitialized(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_INITIALIZED, false)
    }

    fun markInitialized(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_INITIALIZED, true).commit()
    }

    // --- Habits ---
    fun saveHabits(context: Context, habits: List<Habit>) {
        try {
            val array = JSONArray()
            for (habit in habits) {
                val obj = JSONObject().apply {
                    put("id", habit.id)
                    put("name", habit.name)
                    put("description", habit.description)
                    put("category", habit.category)
                    put("colorHex", habit.colorHex)
                    put("iconName", habit.iconName)
                    put("habitType", habit.habitType.name)
                    put("unit", habit.unit)
                    put("targetCount", habit.targetCount)
                    put("recordAmount", habit.recordAmount)
                    put("whenChecking", habit.whenChecking)
                    put("startDate", habit.startDate)
                    put("goalDays", habit.goalDays)
                    put("constantReminder", habit.constantReminder)
                    put("autoPopUpLog", habit.autoPopUpLog)
                    put("currentCountToday", habit.currentCountToday)
                    put("isCompletedToday", habit.isCompletedToday)
                    put("streakDays", habit.streakDays)
                    put("totalDaysCompleted", habit.totalDaysCompleted)
                    put("frequency", habit.frequency)
                    put("timeOfDay", habit.timeOfDay)
                    put("quote", habit.quote)
                    put("reminderTime", habit.reminderTime ?: JSONObject.NULL)

                    val dowArr = JSONArray()
                    habit.daysOfWeek.forEach { dowArr.put(it) }
                    put("daysOfWeek", dowArr)

                    val compArr = JSONArray()
                    habit.completedDates.forEach { compArr.put(it) }
                    put("completedDates", compArr)

                    val notesObj = JSONObject()
                    habit.notes.forEach { (k, v) -> notesObj.put(k, v) }
                    put("notes", notesObj)

                    val stepsArr = JSONArray()
                    habit.checklistSteps.forEach { stepsArr.put(it) }
                    put("checklistSteps", stepsArr)
                }
                array.put(obj)
            }
            getPrefs(context).edit().putString(KEY_HABITS, array.toString()).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadHabits(context: Context): List<Habit>? {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_HABITS)) return null
        val jsonStr = prefs.getString(KEY_HABITS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Habit>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val dow = mutableSetOf<Int>()
                val dowArr = obj.optJSONArray("daysOfWeek")
                if (dowArr != null) {
                    for (j in 0 until dowArr.length()) dow.add(dowArr.getInt(j))
                } else {
                    dow.addAll(setOf(1, 2, 3, 4, 5, 6, 7))
                }

                val comp = mutableSetOf<String>()
                val compArr = obj.optJSONArray("completedDates")
                if (compArr != null) {
                    for (j in 0 until compArr.length()) comp.add(compArr.getString(j))
                }

                val notes = mutableMapOf<String, String>()
                val notesObj = obj.optJSONObject("notes")
                if (notesObj != null) {
                    val keys = notesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        notes[key] = notesObj.optString(key, "")
                    }
                }

                val steps = mutableListOf<String>()
                val stepsArr = obj.optJSONArray("checklistSteps")
                if (stepsArr != null) {
                    for (j in 0 until stepsArr.length()) steps.add(stepsArr.getString(j))
                }

                val typeStr = obj.optString("habitType", HabitType.NORMAL.name)
                val type = try {
                    HabitType.valueOf(typeStr)
                } catch (_: Exception) {
                    HabitType.NORMAL
                }

                list.add(
                    Habit(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", "Habit"),
                        description = obj.optString("description", ""),
                        category = obj.optString("category", "Habits"),
                        colorHex = obj.optString("colorHex", "#0A84FF"),
                        iconName = obj.optString("iconName", "book"),
                        habitType = type,
                        unit = obj.optString("unit", "Count"),
                        targetCount = obj.optInt("targetCount", 1),
                        recordAmount = obj.optInt("recordAmount", 1),
                        whenChecking = obj.optString("whenChecking", "Auto"),
                        startDate = obj.optString("startDate", "Today"),
                        goalDays = obj.optString("goalDays", "Forever"),
                        constantReminder = obj.optBoolean("constantReminder", false),
                        autoPopUpLog = obj.optBoolean("autoPopUpLog", false),
                        currentCountToday = obj.optInt("currentCountToday", 0),
                        isCompletedToday = obj.optBoolean("isCompletedToday", false),
                        streakDays = obj.optInt("streakDays", 0),
                        totalDaysCompleted = obj.optInt("totalDaysCompleted", 0),
                        frequency = obj.optString("frequency", "Daily"),
                        timeOfDay = obj.optString("timeOfDay", "Anytime"),
                        quote = obj.optString("quote", "Show up for yourself."),
                        reminderTime = if (obj.isNull("reminderTime")) null else obj.optString("reminderTime", null),
                        daysOfWeek = dow,
                        completedDates = comp,
                        notes = notes,
                        checklistSteps = steps
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- Tasks ---
    fun saveTasks(context: Context, tasks: List<Task>) {
        val array = JSONArray()
        for (task in tasks) {
            val obj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("description", task.description)
                put("dueTime", task.dueTime)
                put("dueDate", task.dueDate)
                put("category", task.category)
                put("isCompleted", task.isCompleted)
                put("priority", task.priority)
                put("reminder", task.reminder ?: JSONObject.NULL)
                put("repeat", task.repeat ?: JSONObject.NULL)
            }
            array.put(obj)
        }
        try {
            getPrefs(context).edit().putString(KEY_TASKS, array.toString()).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadTasks(context: Context): List<Task>? {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_TASKS)) return null
        val jsonStr = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Task>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Task(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", "Task"),
                        description = obj.optString("description", ""),
                        dueTime = obj.optString("dueTime", "11:00 PM"),
                        dueDate = obj.optString("dueDate", "Today"),
                        category = obj.optString("category", "Personal"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        priority = obj.optInt("priority", 1),
                        reminder = if (obj.isNull("reminder")) null else obj.optString("reminder", null),
                        repeat = if (obj.isNull("repeat")) null else obj.optString("repeat", null)
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- Settings ---
    fun saveSettings(context: Context, settings: AppSettings) {
        val obj = JSONObject().apply {
            put("isDarkMode", settings.isDarkMode)
            put("matchSystem", settings.matchSystem)
            put("accentColorHex", settings.accentColorHex)
            put("bgPattern", settings.bgPattern)
            put("checkStyle", settings.checkStyle)
            put("appLayout", settings.appLayout)
            put("soundEffects", settings.soundEffects)
            put("hapticFeedback", settings.hapticFeedback)
            put("completedTaskStyle", settings.completedTaskStyle)
            put("weekStartsOnMonday", settings.weekStartsOnMonday)
            put("showActivityOnCards", settings.showActivityOnCards)
            put("showQuotes", settings.showQuotes)
            put("connectDays", settings.connectDays)
            put("userName", settings.userName)
            put("userPlan", settings.userPlan)
            put("appLockEnabled", settings.appLockEnabled)
            put("appLockType", settings.appLockType)
            put("appLockPin", settings.appLockPin)
            put("appIcon", settings.appIcon)
            val tabArr = JSONArray()
            settings.tabOrder.forEach { tabArr.put(it) }
            put("tabOrder", tabArr)
        }
        try {
            getPrefs(context).edit().putString(KEY_SETTINGS, obj.toString()).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSettings(context: Context): AppSettings? {
        val jsonStr = getPrefs(context).getString(KEY_SETTINGS, null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            val tabOrder = mutableListOf<String>()
            val tabArr = obj.optJSONArray("tabOrder")
            if (tabArr != null) {
                for (i in 0 until tabArr.length()) tabOrder.add(tabArr.getString(i))
            } else {
                tabOrder.addAll(listOf("Today", "Calendar", "Task", "Habits", "Analysis", "Settings"))
            }

            AppSettings(
                isDarkMode = obj.optBoolean("isDarkMode", true),
                matchSystem = obj.optBoolean("matchSystem", false),
                accentColorHex = obj.optString("accentColorHex", "#0A84FF"),
                bgPattern = obj.optString("bgPattern", "Dots"),
                checkStyle = obj.optString("checkStyle", "Square"),
                appLayout = obj.optString("appLayout", "Modern Grid"),
                soundEffects = obj.optBoolean("soundEffects", true),
                hapticFeedback = obj.optBoolean("hapticFeedback", true),
                completedTaskStyle = obj.optString("completedTaskStyle", "Fade"),
                weekStartsOnMonday = obj.optBoolean("weekStartsOnMonday", true),
                showActivityOnCards = obj.optBoolean("showActivityOnCards", true),
                showQuotes = obj.optBoolean("showQuotes", true),
                connectDays = obj.optBoolean("connectDays", false),
                userName = obj.optString("userName", "Inlitx"),
                userPlan = obj.optString("userPlan", "Pro Lifetime"),
                tabOrder = tabOrder,
                appLockEnabled = obj.optBoolean("appLockEnabled", false),
                appLockType = obj.optString("appLockType", "Biometric / Phone Lock"),
                appLockPin = obj.optString("appLockPin", ""),
                appIcon = obj.optString("appIcon", "Default")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Sections ---
    fun saveSections(context: Context, sections: List<String>) {
        val array = JSONArray()
        sections.forEach { array.put(it) }
        try {
            getPrefs(context).edit().putString(KEY_SECTIONS, array.toString()).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSections(context: Context): List<String>? {
        val jsonStr = getPrefs(context).getString(KEY_SECTIONS, null) ?: return null
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) list.add(array.getString(i))
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
