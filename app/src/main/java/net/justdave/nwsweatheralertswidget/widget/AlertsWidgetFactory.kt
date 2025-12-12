package net.justdave.nwsweatheralertswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
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
    private var alerts: List<NWSAlert> = emptyList()

    override fun onCreate() {
        // Connect to data source
    }

    override fun onDataSetChanged() {
        runBlocking {
            val serializedAlerts = loadAlerts(context, appWidgetId)
            alerts = Json.decodeFromString(serializedAlerts)
        }
    }

    override fun onDestroy() {
        // Close data source
    }

    override fun getCount(): Int {
        return alerts.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.alerts_widget_list_item)
        views.setTextViewText(R.id.alert_item_text, alerts[position].getEvent())
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
        return true
    }
}
