package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Lavoro", "Studio", "Tempo Libero", or custom
    val priority: String, // "Alta", "Media", "Bassa"
    val deadline: Long, // timestamp
    val scheduledStartTime: Long, // timestamp
    val scheduledEndTime: Long, // timestamp
    val actualStartTime: Long? = null, // timestamp
    val actualEndTime: Long? = null, // timestamp
    val completed: Boolean = false,
    val tags: String = "", // comma-separated tags, e.g. "importante, design"
    val attachments: String = "", // comma-separated simulated attachments, e.g. "report.pdf"
    val linkedAppName: String? = null, // e.g. "Slack"
    val linkedAppPackage: String? = null, // e.g. "com.slack"
    val completedOnTime: String? = null, // "Anticipo", "In Orario", "Ritardo", "Non Svolta"
    val timeDifferenceMinutes: Int = 0, // how many minutes earlier (-) or later (+) than scheduledEndTime
    val warningNotified: Boolean = false // to keep track of missed warning alerts
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskTitle: String?,
    val durationMinutes: Int,
    val timestamp: Long, // when completed
    val category: String // category matching the task or pomodoro category
)

@Entity(tableName = "category_links", primaryKeys = ["category", "packageName"])
data class CategoryLinkEntity(
    val category: String, // "Lavoro", "Studio", "Tempo Libero"
    val appName: String, // e.g. "Slack", "Zoom", "YouTube"
    val packageName: String // e.g. "com.slack", "com.zoom", "com.google.android.youtube"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val icon: String // e.g. "💼", "🎓", "🍀", etc.
)

