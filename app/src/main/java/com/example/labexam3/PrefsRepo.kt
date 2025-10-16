package com.example.labexam3

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefsRepo {

    // --- All constants are now in one central place ---
    private const val PREFS_NAME = "WellnessAppPrefs"
    private const val KEY_HABITS = "HabitsList"
    private const val KEY_MOODS = "MoodsList"
    private const val KEY_REMINDER_ON = "HydrationReminderEnabled"

    // --- Habit Functions ---
    fun saveHabits(context: Context, habits: List<Habit>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(habits)
        editor.putString(KEY_HABITS, json)
        editor.apply()
    }

    fun loadHabits(context: Context): MutableList<Habit> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_HABITS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf() // Return an empty list if nothing is saved
        }
    }

    // --- Mood Functions ---
    fun saveMoods(context: Context, moods: List<MoodEntry>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(moods)
        editor.putString(KEY_MOODS, json)
        editor.apply()
    }

    fun loadMoods(context: Context): MutableList<MoodEntry> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_MOODS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // --- Settings Functions ---
    fun setReminderOn(context: Context, isOn: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_REMINDER_ON, isOn).apply()
    }

    fun isReminderOn(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_REMINDER_ON, false)
    }
}