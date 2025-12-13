package net.justdave.nwsweatheralertswidget.objects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import net.justdave.nwsweatheralertswidget.R
import org.json.JSONObject

@Parcelize
@Serializable
data class NWSAlert(
    val rawData: String = "", // Store the raw JSON string
    val headline: String = "",
    val description: String = "",
    val instruction: String = "",
    val id: String = "",
    val sent: String = "",
    val effective: String = "",
    val onset: String = "",
    val expires: String = "",
    val ends: String = "",
    val title: String = "",
    val link: String = "",
    val summary: String = "",
    val event: String = "",
    val status: String = "",
    val msgType: String = "",
    val category: String = "",
    val urgency: String = "",
    val severity: String = "",
    val certainty: String = "",
    val areaDesc: String = ""
) : Parcelable {

    constructor(data: JSONObject? = null) : this(
        rawData = data?.toString() ?: "",
        headline = data?.optJSONObject("properties")?.optString("headline", "Unknown Alert") ?: "Unknown Alert",
        description = data?.optJSONObject("properties")?.optString("description", "No description provided") ?: "No description provided",
        instruction = data?.optJSONObject("properties")?.optString("instruction", "No instructions provided") ?: "No instructions provided",
        id = data?.optJSONObject("properties")?.optString("id", "") ?: "",
        sent = data?.optJSONObject("properties")?.optString("sent", "") ?: "",
        effective = data?.optJSONObject("properties")?.optString("effective", "") ?: "",
        onset = data?.optJSONObject("properties")?.optString("onset", "") ?: "",
        expires = data?.optJSONObject("properties")?.optString("expires", "") ?: "",
        ends = data?.optJSONObject("properties")?.optString("ends", "") ?: "",
        title = data?.optJSONObject("properties")?.optString("event", "") ?: "",
        link = data?.optString("id", "") ?: "",
        summary = data?.optJSONObject("properties")?.optString("description", "") ?: "",
        event = data?.optJSONObject("properties")?.optString("event", "") ?: "",
        status = data?.optJSONObject("properties")?.optString("status", "") ?: "",
        msgType = data?.optJSONObject("properties")?.optString("messageType", "") ?: "",
        category = data?.optJSONObject("properties")?.optString("category", "") ?: "",
        urgency = data?.optJSONObject("properties")?.optString("urgency", "") ?: "",
        severity = data?.optJSONObject("properties")?.optString("severity", "") ?: "",
        certainty = data?.optJSONObject("properties")?.optString("certainty", "") ?: "",
        areaDesc = data?.optJSONObject("properties")?.optString("areaDesc", "") ?: ""
    )

    fun getIcon(): Int {
        var icon = R.drawable.nws_logo
        if (event.contains("Fire") || event.contains("Red Flag")) {
            icon = R.drawable.fire
        }
        if (event.contains("Surf") || event.contains("Tsunami")) {
            icon = R.drawable.wave
        }
        if (event.contains("Winter") || event.contains("Snow")) {
            icon = R.drawable.winter
        }
        if (event.contains("Blizzard")) {
            icon = R.drawable.blizzard
        }
        if (event.contains("Wind")) {
            icon = R.drawable.windy
        }
        if (event.contains("Flood")) {
            icon = R.drawable.flood
        }
        if (event.contains("Ice") || event.contains("Freezing") || event.contains("Freeze") || event.contains("Frost")
            || event.contains("Sleet")) {
            icon = R.drawable.ice
        }
        if (event.contains("Thunderstorm")) {
            icon = R.drawable.thunderstorm
        }
        if (event.contains("Tornado")) {
            icon = R.drawable.tornado
        }
        return icon
    }

    fun getBackground(): Int {
        var background = R.drawable.black_button
        if (event.contains("Fire") || event.contains("Dust")) {
            background = R.drawable.orange_button
        }
        if (event.contains("Winter") || event.contains("Wind") || event.contains("Blizzard") || event.contains("Flood")
            || event.contains("Hydro") || event.contains("Snow") || event.contains("Rain") || event.contains("Marine")
            || event.contains("Surf")) {
            background = R.drawable.blue_button
        }
        if (event.contains("Watch")) {
            background = R.drawable.yellow_button
        }
        if (event.contains("Warning")) {
            background = R.drawable.red_button
        }
        return background
    }

    fun getRawDataForDisplay(): String {
        // Format the raw JSON for display
        return JSONObject(rawData).toString(2)
    }

    override fun toString(): String {
        return headline
    }
}
