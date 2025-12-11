package net.justdave.nwsweatheralertswidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // App has been updated, re-schedule the background worker
            val workRequest = PeriodicWorkRequestBuilder<AlertsUpdateWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "GlobalAlertsUpdateWorker",
                ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
                workRequest
            )
        }
    }
}
