package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String,      // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false, // Zero Pendency workflow tracks pending vs resolved
    val category: String = "Inbox", // Inbox, Work, Personal, Meeting
    val dueDate: String = "",   // E.g., "Today", "Tomorrow", "May 25"
    val assignedTo: String = "", // Stores the name/email of the assigned employee
    val createdAt: Long = System.currentTimeMillis()
)
