package com.example.labexam3

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Get data from SharedPreferences
            val prefs = context.getSharedPreferences("WellnessAppPrefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = prefs.getString("HabitsList", null)
            val type = object : TypeToken<List<Habit>>() {}.type
            val habits: List<Habit> = if (json != null) gson.fromJson(json, type) else emptyList()

            // Calculate Completion Percentage
            val totalHabits = habits.size
            val completedHabits = habits.count { it.isCompleted }
            val percentage = if (totalHabits > 0) {
                (completedHabits.toDouble() / totalHabits * 100).toInt()
            } else {
                0
            }

            // Update the Widget's Views
            val views = RemoteViews(context.packageName, R.layout.habit_widget_layout)
            views.setTextViewText(R.id.textViewWidgetPercentage, "$percentage%")
            views.setProgressBar(R.id.progressBarWidget, 100, percentage, false)


            // Make the widget clickable to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}