package com.sedsoftware.blinkly.domain.model

data class BlinklyDatabaseSnapshot(
    val exercises: List<Exercise>,
    val achievements: List<Achievement>,
    val reminderSchedules: List<ReminderSchedule>,
    val reminders: List<Reminder>,
)
