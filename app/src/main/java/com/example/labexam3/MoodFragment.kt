package com.example.labexam3

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var moodAdapter: MoodAdapter
    private var moodList = mutableListOf<MoodEntry>()
    private lateinit var moodChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        // Load moods from the central repository
        moodList = PrefsRepo.loadMoods(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewMoods)
        moodAdapter = MoodAdapter(moodList) { moodEntry ->
            showDeleteMoodConfirmationDialog(moodEntry)
        }
        recyclerView.adapter = moodAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        moodChart = view.findViewById(R.id.moodChart)
        setupMoodChart()

        view.findViewById<TextView>(R.id.emojiHappy).setOnClickListener { logMood("😊") }
        view.findViewById<TextView>(R.id.emojiOkay).setOnClickListener { logMood("😐") }
        view.findViewById<TextView>(R.id.emojiSad).setOnClickListener { logMood("😔") }
        view.findViewById<TextView>(R.id.emojiAngry).setOnClickListener { logMood("😠") }

        val shareButton: ImageButton = view.findViewById(R.id.buttonShare)
        shareButton.setOnClickListener {
            shareMoodSummary()
        }

        return view
    }

    private fun showDeleteMoodConfirmationDialog(moodEntry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry from ${moodEntry.timestamp}?")
            .setPositiveButton("Delete") { _, _ ->
                val position = moodList.indexOf(moodEntry)
                if (position != -1) {
                    moodList.removeAt(position)
                    moodAdapter.notifyDataSetChanged()
                    saveMoods()
                    setupMoodChart()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logMood(emoji: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
        val timestamp = dateFormat.format(calendar.time)
        val newMood = MoodEntry(System.currentTimeMillis(), emoji, timestamp)
        moodList.add(newMood)
        saveMoods()
        moodAdapter.notifyDataSetChanged()
        setupMoodChart()
        Toast.makeText(context, "Mood logged!", Toast.LENGTH_SHORT).show()
    }

    private fun saveMoods() {
        // Save moods using the central repository
        PrefsRepo.saveMoods(requireContext(), moodList)
    }

    private fun shareMoodSummary() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.timeInMillis
        val recentMoods = moodList.filter { it.id >= sevenDaysAgo }

        if (recentMoods.isEmpty()) {
            Toast.makeText(context, "Not enough mood data to share.", Toast.LENGTH_SHORT).show()
            return
        }

        val appName = getString(R.string.app_name)
        val summary = StringBuilder("Here's my mood summary for the week from my $appName app:\n\n")
        recentMoods.forEach {
            summary.append("• ${it.timestamp}: Feeling ${it.emoji}\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summary.toString())
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share your mood summary via...")
        startActivity(shareIntent)
    }

    private fun setupMoodChart() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.timeInMillis
        val recentMoods = moodList.filter { it.id >= sevenDaysAgo }.sortedBy { it.id }

        moodChart.description.isEnabled = false
        moodChart.legend.isEnabled = false
        moodChart.setTouchEnabled(false)
        moodChart.setDrawGridBackground(false)
        moodChart.setNoDataText("")

        if (recentMoods.isEmpty()) {
            moodChart.visibility = View.INVISIBLE
            moodChart.data = null
            moodChart.invalidate()
            return
        } else {
            moodChart.visibility = View.VISIBLE
        }

        val entries = ArrayList<Entry>()
        val dateLabels = ArrayList<String>()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        recentMoods.forEachIndexed { index, moodEntry ->
            val moodValue = when (moodEntry.emoji) {
                "😊" -> 4f
                "😐" -> 3f
                "😔" -> 2f
                "😠" -> 1f
                else -> 0f
            }
            entries.add(Entry(index.toFloat(), moodValue))
            dateLabels.add(dateFormat.format(Date(moodEntry.id)))
        }

        val dataSet = LineDataSet(entries, "Mood Level").apply {
            val primaryColor = ContextCompat.getColor(requireContext(), R.color.midnight_bloom_primary)
            color = primaryColor
            setCircleColor(primaryColor)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            val fillGradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#80A855F7"), Color.TRANSPARENT)
            )
            fillDrawable = fillGradient
        }

        moodChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.midnight_bloom_onSurfaceVariant)
        moodChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.midnight_bloom_onSurfaceVariant)

        moodChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.midnight_bloom_surface)
            labelRotationAngle = -45f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value.toInt() >= 0 && value.toInt() < dateLabels.size) {
                        dateLabels[value.toInt()]
                    } else ""
                }
            }
        }

        moodChart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#33AEAEB2")
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.midnight_bloom_surface)
            axisMinimum = 0f
            axisMaximum = 5f
            granularity = 1f
            setLabelCount(6, true)
        }
        moodChart.axisRight.isEnabled = false

        val lineData = LineData(dataSet)
        moodChart.data = lineData
        moodChart.invalidate()
    }
}