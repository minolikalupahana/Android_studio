package com.example.labexam3

data class Habit(
    val id: Long, // Unique ID for each habit
    var name: String,
    var isCompleted: Boolean = false
)