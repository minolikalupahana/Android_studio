package com.example.labexam3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onItemToggled: () -> Unit,
    private val onItemLongClicked: (Habit) -> Unit,
    private val onItemClicked: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val currentHabit = habits[position]
        holder.habitName.text = currentHabit.name
        holder.checkBox.isChecked = currentHabit.isCompleted

        // Checkbox eka wenas kirimedi save
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            currentHabit.isCompleted = isChecked
            onItemToggled()
        }

        // Item  long press kala wita delete
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(currentHabit)
            true // event was consumed
        }

        // Item  click kala wita edit
        holder.itemView.setOnClickListener {
            onItemClicked(currentHabit)
        }
    }

    override fun getItemCount() = habits.size
}