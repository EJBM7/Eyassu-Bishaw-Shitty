package com.example.model

data class Habit(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String = "Habits",
    val colorHex: String = "#FF6D00", // Orange by default
    val iconName: String = "book",    // book, heart, fitness, etc.
    val habitType: HabitType = HabitType.NORMAL,
    val unit: String = "Count",
    val targetCount: Int = 1,
    val recordAmount: Int = 1,
    val whenChecking: String = "Auto", // "Auto", "Manual", "Complete"
    val startDate: String = "Sep 20",
    val goalDays: String = "Forever",
    val constantReminder: Boolean = false,
    val autoPopUpLog: Boolean = false,
    val currentCountToday: Int = 0,
    val isCompletedToday: Boolean = false,
    val streakDays: Int = 0,
    val totalDaysCompleted: Int = 0,
    val frequency: String = "Daily",
    val timeOfDay: String = "Anytime",
    val quote: String = "Show up for yourself.",
    val reminderTime: String? = null,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val completedDates: Set<String> = emptySet(), // "YYYY-MM-DD"
    val notes: Map<String, String> = emptyMap(),
    val checklistSteps: List<String> = emptyList()
)

enum class HabitType {
    NORMAL, AVOID, AMOUNT
}

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val dueTime: String = "11:00 PM",
    val dueDate: String = "Today",
    val category: String = "Personal",
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 0: None, 1: Low, 2: Medium, 3: High
    val reminder: String? = null,
    val repeat: String? = null
)

data class AppSettings(
    val isDarkMode: Boolean = true,
    val matchSystem: Boolean = false,
    val accentColorHex: String = "#0A84FF",
    val bgPattern: String = "Dots", // "Dots", "SquarePaper", "Grid", "None"
    val checkStyle: String = "Square", // "Circle", "Square"
    val appLayout: String = "Modern Grid", // "Modern Grid", "Classic List"
    val soundEffects: Boolean = true,
    val hapticFeedback: Boolean = true,
    val completedTaskStyle: String = "Fade", // "Fade", "Crossline"
    val weekStartsOnMonday: Boolean = true,
    val showActivityOnCards: Boolean = true,
    val showQuotes: Boolean = true,
    val connectDays: Boolean = false,
    val userName: String = "Inlitx",
    val userPlan: String = "Pro Lifetime",
    val tabOrder: List<String> = listOf("Today", "Calendar", "Task", "Habits", "Analysis", "Settings"),
    val appLockEnabled: Boolean = false,
    val appLockType: String = "Biometric / Phone Lock", // "Biometric / Phone Lock", "PIN"
    val appLockPin: String = "",
    val appIcon: String = "Default" // "Default", "Green", "Tricolor"
)

enum class MainTab {
    TODAY, CALENDAR, TASKS, HABITS, ANALYSIS, SETTINGS
}

enum class TimeRangeView {
    WEEK, MONTH, YEAR
}
