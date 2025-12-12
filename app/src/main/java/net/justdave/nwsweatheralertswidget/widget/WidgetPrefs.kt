package net.justdave.nwsweatheralertswidget.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.justdave.nwsweatheralertswidget.R

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Functions for saving and loading the widget's configuration (title, area, zone)
suspend fun saveWidgetPrefs(context: Context, appWidgetId: Int, areaId: String, zoneId: String, title: String) {
    context.dataStore.edit {
        it[stringPreferencesKey("appwidget_area_$appWidgetId")] = areaId
        it[stringPreferencesKey("appwidget_zone_$appWidgetId")] = zoneId
        it[stringPreferencesKey("appwidget_title_$appWidgetId")] = title
    }
}

suspend fun loadWidgetPrefs(context: Context, appWidgetId: Int): Map<String, String?> {
    return context.dataStore.data.map {
        mapOf(
            "area" to it[stringPreferencesKey("appwidget_area_$appWidgetId")],
            "zone" to it[stringPreferencesKey("appwidget_zone_$appWidgetId")],
            "title" to it[stringPreferencesKey("appwidget_title_$appWidgetId")]
        )
    }.first()
}

// Functions for saving and loading the fetched alerts for a specific widget
suspend fun saveAlerts(context: Context, appWidgetId: Int, alerts: String) {
    context.dataStore.edit {
        it[stringPreferencesKey("appwidget_alerts_$appWidgetId")] = alerts
    }
}

suspend fun loadAlerts(context: Context, appWidgetId: Int): String {
    return context.dataStore.data.map {
        it[stringPreferencesKey("appwidget_alerts_$appWidgetId")] ?: "[]"
    }.first()
}

// Function to clean up all preferences for a deleted widget
suspend fun deleteWidgetPrefs(context: Context, appWidgetId: Int) {
    context.dataStore.edit {
        it.remove(stringPreferencesKey("appwidget_area_$appWidgetId"))
        it.remove(stringPreferencesKey("appwidget_zone_$appWidgetId"))
        it.remove(stringPreferencesKey("appwidget_title_$appWidgetId"))
        it.remove(stringPreferencesKey("appwidget_alerts_$appWidgetId"))
    }
}
