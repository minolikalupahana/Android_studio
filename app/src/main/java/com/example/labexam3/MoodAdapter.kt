package com.example.labexam3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onItemLongClicked: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiTextView: TextView = itemView.findViewById(R.id.textViewEmoji)
        val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val currentEntry = moodEntries[moodEntries.size - 1 - position]
        holder.emojiTextView.text = currentEntry.emoji
        holder.timestampTextView.text = currentEntry.timestamp

        holder.itemView.setOnLongClickListener {
            onItemLongClicked(currentEntry)
            true
        }
    }

    override fun getItemCount() = moodEntries.size
}