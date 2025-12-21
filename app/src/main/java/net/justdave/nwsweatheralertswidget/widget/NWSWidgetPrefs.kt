package net.justdave.nwsweatheralertswidget.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.justdave.nwsweatheralertswidget.lenientJson
import net.justdave.nwsweatheralertswidget.objects.NWSAlert

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Define preference keys as functions to ensure uniqueness
private fun areaKey(appWidgetId: Int) = stringPreferencesKey("appwidget_area_$appWidgetId")
private fun zoneKey(appWidgetId: Int) = stringPreferencesKey("appwidget_zone_$appWidgetId")
private fun themeKey(appWidgetId: Int) = stringPreferencesKey("appwidget_theme_$appWidgetId")
private fun alertsKey(appWidgetId: Int) = stringPreferencesKey("appwidget_alerts_$appWidgetId")
private fun updatedKey(appWidgetId: Int) = stringPreferencesKey("appwidget_updated_$appWidgetId")

// Functions for saving and loading the widget's configuration (area, zone, theme)
suspend fun saveWidgetPrefs(context: Context, appWidgetId: Int, areaId: String, zoneId: String, theme: String) {
    context.dataStore.edit {
        it[areaKey(appWidgetId)] = areaId
        it[zoneKey(appWidgetId)] = zoneId
        it[themeKey(appWidgetId)] = theme
    }
}

suspend fun loadWidgetPrefs(context: Context, appWidgetId: Int): Map<String, String?> {
    return context.dataStore.data.map { prefs ->
        val map = mutableMapOf<String, String?>()
        val alerts = prefs[alertsKey(appWidgetId)] ?: "[]"
        map["area"] = prefs[areaKey(appWidgetId)]
        map["zone"] = prefs[zoneKey(appWidgetId)]
        map["theme"] = prefs[themeKey(appWidgetId)] ?: "semitransparent"
        map["updated"] = prefs[updatedKey(appWidgetId)]
        map["alert_count"] = lenientJson.decodeFromString<List<NWSAlert>>(alerts).size.toString()
        map
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
        it.remove(themeKey(appWidgetId))
        it.remove(alertsKey(appWidgetId))
        it.remove(updatedKey(appWidgetId))
    }
}
