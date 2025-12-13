package net.justdave.nwsweatheralertswidget.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.debugDataStore: DataStore<Preferences> by preferencesDataStore(name = "debug_settings")

private val KEY_AREA = stringPreferencesKey("debug_area")
private val KEY_ZONE = stringPreferencesKey("debug_zone")

suspend fun saveDebugPrefs(context: Context, areaId: String, zoneId: String) {
    context.debugDataStore.edit {
        it[KEY_AREA] = areaId
        it[KEY_ZONE] = zoneId
    }
}

suspend fun loadDebugPrefs(context: Context): Pair<String, String> {
    return context.debugDataStore.data.map {
        val area = it[KEY_AREA] ?: "us-all"
        val zone = it[KEY_ZONE] ?: "all"
        Pair(area, zone)
    }.first()
}
