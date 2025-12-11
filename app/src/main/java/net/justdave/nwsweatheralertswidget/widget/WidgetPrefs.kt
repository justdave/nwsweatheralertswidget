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

suspend fun saveWidgetPrefs(context: Context, appWidgetId: Int, areaId: String, zoneId: String, title: String) {
    val areaKey = stringPreferencesKey("appwidget_area_$appWidgetId")
    val zoneKey = stringPreferencesKey("appwidget_zone_$appWidgetId")
    val titleKey = stringPreferencesKey("appwidget_title_$appWidgetId")
    context.dataStore.edit { settings ->
        settings[areaKey] = areaId
        settings[zoneKey] = zoneId
        settings[titleKey] = title
    }
}

suspend fun loadWidgetPrefs(context: Context, appWidgetId: Int): Map<String, String> {
    val areaKey = stringPreferencesKey("appwidget_area_$appWidgetId")
    val zoneKey = stringPreferencesKey("appwidget_zone_$appWidgetId")
    val titleKey = stringPreferencesKey("appwidget_title_$appWidgetId")
    val flow = context.dataStore.data.map {
        mapOf(
            "area" to (it[areaKey] ?: "us-all"),
            "zone" to (it[zoneKey] ?: "all"),
            "title" to (it[titleKey] ?: context.getString(R.string.appwidget_text))
        )
    }
    return flow.first()
}

suspend fun saveAlerts(context: Context, appWidgetId: Int, alerts: String) {
    val alertsKey = stringPreferencesKey("appwidget_alerts_$appWidgetId")
    context.dataStore.edit { settings ->
        settings[alertsKey] = alerts
    }
}

suspend fun loadAlerts(context: Context, appWidgetId: Int): String {
    val alertsKey = stringPreferencesKey("appwidget_alerts_$appWidgetId")
    val flow = context.dataStore.data.map {
        it[alertsKey] ?: "[]"
    }
    return flow.first()
}

suspend fun deleteWidgetPrefs(context: Context, appWidgetId: Int) {
    val areaKey = stringPreferencesKey("appwidget_area_$appWidgetId")
    val zoneKey = stringPreferencesKey("appwidget_zone_$appWidgetId")
    val titleKey = stringPreferencesKey("appwidget_title_$appWidgetId")
    val alertsKey = stringPreferencesKey("appwidget_alerts_$appWidgetId")
    context.dataStore.edit {
        it.remove(areaKey)
        it.remove(zoneKey)
        it.remove(titleKey)
        it.remove(alertsKey)
    }
}
