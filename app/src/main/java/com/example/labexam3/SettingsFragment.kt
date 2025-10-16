package com.example.labexam3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private val WORKER_TAG = "hydrationReminderWorker"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val reminderSwitch: SwitchMaterial = view.findViewById(R.id.switchHydration)

        // Load state from the central repository
        reminderSwitch.isChecked = PrefsRepo.isReminderOn(requireContext())

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save state using the central repository
            PrefsRepo.setReminderOn(requireContext(), isChecked)

            if (isChecked) {
                startReminder()
                Toast.makeText(context, R.string.reminder_on, Toast.LENGTH_SHORT).show()
            } else {
                cancelReminder()
                Toast.makeText(context, R.string.reminder_off, Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun startReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        scheduleWork()
    }

    private fun scheduleWork() {
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(2, TimeUnit.HOURS).build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private fun cancelReminder() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork(WORKER_TAG)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                scheduleWork()
            }
        }
}