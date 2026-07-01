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
    val areaDesc: String = "",
    val maxWindGust: String = "",
    val maxHailSize: String = "",
    val thunderstormDamageThreat: String = "",
    val tornadoDetection: String = "",
    val hailThreat: String = "",
    val windThreat: String = ""
) : Parcelable {

    constructor(data: JSONObject? = null) : this(
        rawData = data?.toString() ?: "",
        headline = data?.optJSONObject("properties")?.optString("headline", "").sanitizeNull() ?: "",
        description = data?.optJSONObject("properties")?.optString("description", "").sanitizeNull() ?: "",
        instruction = data?.optJSONObject("properties")?.optString("instruction", "").sanitizeNull() ?: "",
        id = data?.optJSONObject("properties")?.optString("id", "").sanitizeNull() ?: "",
        sent = data?.optJSONObject("properties")?.optString("sent", "").sanitizeNull() ?: "",
        effective = data?.optJSONObject("properties")?.optString("effective", "").sanitizeNull() ?: "",
        onset = data?.optJSONObject("properties")?.optString("onset", "").sanitizeNull() ?: "",
        expires = data?.optJSONObject("properties")?.optString("expires", "").sanitizeNull() ?: "",
        ends = data?.optJSONObject("properties")?.optString("ends", "").sanitizeNull() ?: "",
        title = data?.optJSONObject("properties")?.optString("event", "").sanitizeNull() ?: "",
        link = data?.optString("id", "").sanitizeNull() ?: "",
        summary = data?.optJSONObject("properties")?.optString("description", "").sanitizeNull() ?: "",
        event = data?.optJSONObject("properties")?.optString("event", "").sanitizeNull() ?: "",
        status = data?.optJSONObject("properties")?.optString("status", "").sanitizeNull() ?: "",
        msgType = data?.optJSONObject("properties")?.optString("messageType", "").sanitizeNull() ?: "",
        category = data?.optJSONObject("properties")?.optString("category", "").sanitizeNull() ?: "",
        urgency = data?.optJSONObject("properties")?.optString("urgency", "").sanitizeNull() ?: "",
        severity = data?.optJSONObject("properties")?.optString("severity", "").sanitizeNull() ?: "",
        certainty = data?.optJSONObject("properties")?.optString("certainty", "").sanitizeNull() ?: "",
        areaDesc = data?.optJSONObject("properties")?.optString("areaDesc", "").sanitizeNull() ?: "",
        maxWindGust = data?.optJSONObject("properties")?.optJSONObject("parameters")?.optJSONArray("maxWindGust")?.optString(0, "").sanitizeNull() ?: "",
        maxHailSize = data?.optJSONObject("properties")?.optJSONObject("parameters")?.optJSONArray("maxHailSize")?.optString(0, "").sanitizeNull() ?: "",
        thunderstormDamageThreat = data?.optJSONObject("properties")?.optJSONObject("parameters")?.optJSONArray("thunderstormDamageThreat")?.optString(0, "").sanitizeNull() ?: "",
        tornadoDetection = data?.optJSONObject("properties")?.optJSONObject("parameters")?.optJSONArray("tornadoDetection")?.optString(0, "").sanitizeNull() ?: "",
        hailThreat = data?.optJSONObject("properties")?.optJSONObject("parameters")?.optJSONArray("hailThreat")?.optString(0, "").sanitizeNull() ?: "",
        windThreat = data?.optJSONObject("properties")?.optJSONObject("parameters")?.optJSONArray("windThreat")?.optString(0, "").sanitizeNull() ?: ""
    )

    fun getIcon(): Int {
        var icon = R.drawable.nws_logo
        val eventLower = event.lowercase()
        if (eventLower.contains("fire") || eventLower.contains("red flag")) {
            icon = R.drawable.fire
        }
        if (eventLower.contains("surf") || eventLower.contains("tsunami") || eventLower.contains("hazardous seas")) {
            icon = R.drawable.wave
        }
        if (eventLower.contains("winter") || eventLower.contains("snow")) {
            icon = R.drawable.winter
        }
        if (eventLower.contains("blizzard")) {
            icon = R.drawable.blizzard
        }
        if (eventLower.contains("wind") || eventLower.contains("gale")) {
            icon = R.drawable.windy
        }
        if ((eventLower.contains("hurricane") || eventLower.contains("tropical") || eventLower.contains("typhoon")) && !eventLower.contains("wind")) {
            icon = R.drawable.hurricane
        }
        if (eventLower.contains("flood")) {
            icon = R.drawable.flood
        }
        if (eventLower.contains("water") || eventLower.contains("hydrologic")) {
            icon = R.drawable.water
        }
        if (eventLower.contains("ice") || eventLower.contains("freezing") || eventLower.contains("freeze") || eventLower.contains("frost")
            || eventLower.contains("sleet") || eventLower.contains("cold")) {
            icon = R.drawable.ice
        }
        if (eventLower.contains("thunderstorm")) {
            icon = R.drawable.thunderstorm
        }
        if (eventLower.contains("tornado")) {
            icon = R.drawable.tornado
        }
        if (eventLower.contains("volcano") || eventLower.contains("ashfall")) {
            icon = R.drawable.volcano
        }
        if (eventLower.contains("heat") || eventLower.contains("temperature")) {
            icon = R.drawable.heat
        }
        return icon
    }

    fun getBackground(): Int {
        var background = R.drawable.grey_button
        val eventLower = event.lowercase()
        if (eventLower.contains("warning")) {
            return R.drawable.red_button
        }
        if (eventLower.contains("watch")) {
            return R.drawable.yellow_button
        }
        if (eventLower.contains("fire") || eventLower.contains("dust") || eventLower.contains("heat") || eventLower.contains("advisory")) {
            background = R.drawable.orange_button
        }
        if (eventLower.contains("winter") || eventLower.contains("wind") || eventLower.contains("blizzard") || eventLower.contains("flood")
            || eventLower.contains("hydro") || eventLower.contains("snow") || eventLower.contains("rain") || eventLower.contains("marine")
            || eventLower.contains("surf")) {
            background = R.drawable.blue_button
        }
        return background
    }

    fun getBackgroundColor(): Int {
        val eventLower = event.lowercase()
        if (eventLower.contains("warning")) {
            return R.color.nws_red
        }
        if (eventLower.contains("watch")) {
            return R.color.nws_yellow
        }
        if (eventLower.contains("fire") || eventLower.contains("dust") || eventLower.contains("heat") || eventLower.contains("advisory")) {
            return R.color.nws_orange
        }
        if (eventLower.contains("winter") || eventLower.contains("wind") || eventLower.contains("blizzard") || eventLower.contains("flood")
            || eventLower.contains("hydro") || eventLower.contains("snow") || eventLower.contains("rain") || eventLower.contains("marine")
            || eventLower.contains("surf")) {
            return R.color.nws_blue
        }
        return R.color.nws_grey
    }

    fun getRawDataForDisplay(): String {
        // Format the raw JSON for display
        return JSONObject(rawData).toString(2)
    }

    fun getSmartDescription(): String = smartUnwrap(description)
    fun getSmartInstruction(): String = smartUnwrap(instruction)

    private fun smartUnwrap(text: String): String {
        if (text.isEmpty() || text == "null" || text == "No instructions provided" || text == "No description provided") return ""
        // Replace single newlines with spaces, but keep double newlines (paragraphs)
        // and keep newlines that are followed by a bullet point (* or -).
        // and keep newlines that are preceded by an ellipsis (...).
        // Also normalize line endings to \n
        return text.replace("\r\n", "\n")
            .replace(Regex("(?<!\\n|\\.{3})\\n(?!\\n|\\s*[*•\\-])"), " ")
            .trim()
    }

    override fun toString(): String {
        return headline
    }
}

private fun String?.sanitizeNull(): String? {
    return if (this == "null") "" else this
}
