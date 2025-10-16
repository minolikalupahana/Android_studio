package com.example.labexam3

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class HabitsFragment : Fragment() {

    private lateinit var habitAdapter: HabitAdapter
    private var habitList = mutableListOf<Habit>()
    private lateinit var konfettiView: KonfettiView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)
        konfettiView = view.findViewById(R.id.konfettiView)

        // Load habits from the central repository
        habitList = PrefsRepo.loadHabits(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewHabits)
        habitAdapter = HabitAdapter(
            habits = habitList,
            onItemToggled = { saveHabits() },
            onItemLongClicked = { habit -> showDeleteConfirmationDialog(habit) },
            onItemClicked = { habit -> showEditHabitDialog(habit) }
        )
        recyclerView.adapter = habitAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = view.findViewById(R.id.fabAddHabit)
        fab.setOnClickListener { showAddHabitDialog() }

        return view
    }

    private fun showDeleteConfirmationDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                val position = habitList.indexOf(habit)
                if (position != -1) {
                    habitList.removeAt(position)
                    habitAdapter.notifyItemRemoved(position)
                    saveHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_habit, null)
        val editText = dialogLayout.findViewById<TextInputEditText>(R.id.editTextHabitName)

        editText.setText(habit.name)

        with(builder) {
            setTitle("Edit Habit")
            setPositiveButton("Save") { _, _ ->
                val newHabitName = editText.text.toString()
                if (newHabitName.isNotEmpty()) {
                    val position = habitList.indexOf(habit)
                    if (position != -1) {
                        habitList[position].name = newHabitName
                        habitAdapter.notifyItemChanged(position)
                        saveHabits()
                    }
                }
            }
                .setNegativeButton("Cancel", null)
                .setView(dialogLayout)
                .show()
        }
    }

    private fun showAddHabitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_habit, null)
        val editText = dialogLayout.findViewById<TextInputEditText>(R.id.editTextHabitName)

        with(builder) {
            setTitle("Add a New Habit")
            setPositiveButton("Add") { _, _ ->
                val habitName = editText.text.toString()
                if (habitName.isNotEmpty()) {
                    val newHabit = Habit(System.currentTimeMillis(), habitName)
                    habitList.add(newHabit)
                    habitAdapter.notifyItemInserted(habitList.size - 1)
                    saveHabits()
                }
            }
                .setNegativeButton("Cancel", null)
                .setView(dialogLayout)
                .show()
        }
    }

    private fun saveHabits() {
        // Save habits using the central repository
        PrefsRepo.saveHabits(requireContext(), habitList)

        updateWidget()
        checkIfAllHabitsCompleted()
    }

    private fun checkIfAllHabitsCompleted() {
        if (habitList.isNotEmpty() && habitList.all { it.isCompleted }) {
            showConfetti()
        }
    }

    private fun showConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        konfettiView.start(party)
    }

    private fun updateWidget() {
        if (context == null) return
        val context = requireContext().applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HabitWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        for (appWidgetId in appWidgetIds) {
            HabitWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}