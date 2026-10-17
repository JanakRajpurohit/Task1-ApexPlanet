package com.example.myapplication

import java.util.UUID

enum class Priority {
    LOW, MEDIUM, HIGH
}

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM
)

enum class TodoFilter {
    ALL, ACTIVE, COMPLETED
}
