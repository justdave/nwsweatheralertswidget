package net.justdave.nwsweatheralertswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.justdave.nwsweatheralertswidget.R
import net.justdave.nwsweatheralertswidget.objects.NWSAlert

class AlertsWidgetFactory(private val context: Context, intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    override fun onCreate() {
        // No-op
    }

    override fun onDataSetChanged() {
        // This is called by the system when notifyAppWidgetViewDataChanged is called.
        // We no longer need to load data here, as it will be loaded on-demand
        // in getCount() and getViewAt() to ensure correctness for each widget.
    }

    override fun onDestroy() {
        // No-op
    }

    private fun getAlertsForThisWidget(): List<NWSAlert> {
        // Helper function to load data synchronously for this specific widget instance
        return runBlocking {
            val serializedAlerts = loadAlerts(context, appWidgetId)
            Json.decodeFromString(serializedAlerts)
        }
    }

    override fun getCount(): Int {
        // Load data on-demand to get the correct count for this specific widget.
        return getAlertsForThisWidget().size
    }

    override fun getViewAt(position: Int): RemoteViews {
        // Load data on-demand to get the correct item for this specific widget.
        val alerts = getAlertsForThisWidget()
        val views = RemoteViews(context.packageName, R.layout.alerts_widget_list_item)

        // Ensure we don't go out of bounds if the data changes between getCount() and here
        if (position < alerts.size) {
            val alert = alerts[position]
            views.setTextViewText(R.id.alert_item_text, alert.getEvent())
            Log.i("AlertsWidgetFactory", "Widget $appWidgetId: loaded view for '${alert.getEvent()}' at position $position")
        }
        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        // The list content can change, so IDs are not stable.
        return false
    }
}
