package net.justdave.nwsweatheralertswidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "Boot completed, starting service")
            val serviceIntent = Intent(context, AlertsUpdateService::class.java).apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.startForegroundService(serviceIntent)
        }
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // App has been updated, start the background service directly
            Log.i("AppUpdateReceiver", "App updated, starting service")
            val serviceIntent = Intent(context, AlertsUpdateService::class.java).apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.startForegroundService(serviceIntent)
        }
    }
}
