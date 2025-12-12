package net.justdave.nwsweatheralertswidget.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import net.justdave.nwsweatheralertswidget.objects.NWSAlert

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Define preference keys as functions to ensure uniqueness
private fun areaKey(appWidgetId: Int) = stringPreferencesKey("appwidget_area_$appWidgetId")
private fun zoneKey(appWidgetId: Int) = stringPreferencesKey("appwidget_zone_$appWidgetId")
private fun titleKey(appWidgetId: Int) = stringPreferencesKey("appwidget_title_$appWidgetId")
private fun alertsKey(appWidgetId: Int) = stringPreferencesKey("appwidget_alerts_$appWidgetId")
private fun updatedKey(appWidgetId: Int) = stringPreferencesKey("appwidget_updated_$appWidgetId")

// Functions for saving and loading the widget's configuration (title, area, zone)
suspend fun saveWidgetPrefs(context: Context, appWidgetId: Int, areaId: String, zoneId: String, title: String) {
    context.dataStore.edit {
        it[areaKey(appWidgetId)] = areaId
        it[zoneKey(appWidgetId)] = zoneId
        it[titleKey(appWidgetId)] = title
    }
}

suspend fun loadWidgetPrefs(context: Context, appWidgetId: Int): Map<String, String?> {
    return context.dataStore.data.map {
        val alerts = it[alertsKey(appWidgetId)] ?: "[]"
        mapOf(
            "area" to it[areaKey(appWidgetId)],
            "zone" to it[zoneKey(appWidgetId)],
            "title" to it[titleKey(appWidgetId)],
            "updated" to it[updatedKey(appWidgetId)],
            "alert_count" to Json.decodeFromString<List<NWSAlert>>(alerts).size.toString()
        )
    }.first()
}

// Functions for saving and loading the fetched alerts for a specific widget
suspend fun saveAlerts(context: Context, appWidgetId: Int, alerts: String) {
    context.dataStore.edit {
        it[alertsKey(appWidgetId)] = alerts
    }
}

suspend fun loadAlerts(context: Context, appWidgetId: Int): String {
    return context.dataStore.data.map {
        it[alertsKey(appWidgetId)] ?: "[]"
    }.first()
}

// Functions for saving the last updated timestamp
suspend fun saveUpdatedTimestamp(context: Context, appWidgetId: Int, timestamp: String) {
    context.dataStore.edit {
        it[updatedKey(appWidgetId)] = timestamp
    }
}

// Function to clean up all preferences for a deleted widget
suspend fun deleteWidgetPrefs(context: Context, appWidgetId: Int) {
    context.dataStore.edit {
        it.remove(areaKey(appWidgetId))
        it.remove(zoneKey(appWidgetId))
        it.remove(titleKey(appWidgetId))
        it.remove(alertsKey(appWidgetId))
        it.remove(updatedKey(appWidgetId))
    }
}
