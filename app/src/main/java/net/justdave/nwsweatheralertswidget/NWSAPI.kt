package net.justdave.nwsweatheralertswidget

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

private const val apiurl = "https://api.weather.gov"
// The documentation for this API is at https://www.weather.gov/documentation/services-web-api

/**
 * Interface with the NWS REST API
 *
 * API documentation can be found at https://www.weather.gov/documentation/services-web-api
 *
 * Since it's a web service, every function that returns data requires a callback to deliver
 * the response.
 */
class NWSAPI constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: NWSAPI? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NWSAPI(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    private fun makeRequest(
        url: String,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ): JsonObjectRequest {
        return object : JsonObjectRequest(
            Method.GET, url, null,
            listener,
            errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] =
                    "(http://justdave.github.io/nwsweatheralertswidget, playstoresupport@justdave.net)"
                return headers
            }
        }
    }

    fun getAreas(listener: Response.Listener<ArrayList<NWSArea>>) {
        val areaList = ArrayList<NWSArea>()

        // Yes, this is hardcoded. No way to get these from the API (yet)
        // we're going to treat it like a queued request anyway just in
        // case they add it someday and I can drop all this code without
        // having to refactor everywhere that calls it.
        areaList.add(NWSArea("us-all", "National (all)"))
        areaList.add(NWSArea("AL", "Alabama"))
        areaList.add(NWSArea("AK", "Alaska"))
        areaList.add(NWSArea("AS", "America Samoa"))
        areaList.add(NWSArea("AR", "Arizona"))
        areaList.add(NWSArea("AZ", "Arkansas"))
        areaList.add(NWSArea("CA", "California"))
        areaList.add(NWSArea("CO", "Colorado"))
        areaList.add(NWSArea("CT", "Connecticut"))
        areaList.add(NWSArea("DE", "Delaware"))
        areaList.add(NWSArea("DC", "District of Columbia"))
        areaList.add(NWSArea("FL", "Florida"))
        areaList.add(NWSArea("GA", "Georgia"))
        areaList.add(NWSArea("GU", "Guam"))
        areaList.add(NWSArea("HI", "Hawaii"))
        areaList.add(NWSArea("ID", "Idaho"))
        areaList.add(NWSArea("IL", "Illinois"))
        areaList.add(NWSArea("IN", "Indiana"))
        areaList.add(NWSArea("IA", "Iowa"))
        areaList.add(NWSArea("KS", "Kansas"))
        areaList.add(NWSArea("KY", "Kentucky"))
        areaList.add(NWSArea("LA", "Louisiana"))
        areaList.add(NWSArea("ME", "Maine"))
        areaList.add(NWSArea("MD", "Maryland"))
        areaList.add(NWSArea("MA", "Massachusetts"))
        areaList.add(NWSArea("MI", "Michigan"))
        areaList.add(NWSArea("MN", "Minnesota"))
        areaList.add(NWSArea("MS", "Mississippi"))
        areaList.add(NWSArea("MO", "Missouri"))
        areaList.add(NWSArea("MT", "Montana"))
        areaList.add(NWSArea("NE", "Nebraska"))
        areaList.add(NWSArea("NV", "Nevada"))
        areaList.add(NWSArea("NH", "New Hampshire"))
        areaList.add(NWSArea("NJ", "New Jersey"))
        areaList.add(NWSArea("NM", "New Mexico"))
        areaList.add(NWSArea("NY", "New York"))
        areaList.add(NWSArea("NC", "North Carolina"))
        areaList.add(NWSArea("ND", "North Dakota"))
        areaList.add(NWSArea("OH", "Ohio"))
        areaList.add(NWSArea("OK", "Oklahoma"))
        areaList.add(NWSArea("OR", "Oregon"))
        areaList.add(NWSArea("PA", "Pennsylvania"))
        areaList.add(NWSArea("PR", "Puerto Rico"))
        areaList.add(NWSArea("RI", "Rhode Island"))
        areaList.add(NWSArea("SC", "South Carolina"))
        areaList.add(NWSArea("SD", "South Dakota"))
        areaList.add(NWSArea("TN", "Tennessee"))
        areaList.add(NWSArea("TX", "Texas"))
        areaList.add(NWSArea("UT", "Utah"))
        areaList.add(NWSArea("VT", "Vermont"))
        areaList.add(NWSArea("VI", "Virgin Islands"))
        areaList.add(NWSArea("VA", "Virginia"))
        areaList.add(NWSArea("WA", "Washington"))
        areaList.add(NWSArea("WV", "West Virginia"))
        areaList.add(NWSArea("WI", "Wisconsin"))
        areaList.add(NWSArea("WY", "Wyoming"))
        areaList.add(NWSArea("PZ", "Eastern North Pacific WA-CA"))
        areaList.add(NWSArea("PK", "North Pacific near Alaska"))
        areaList.add(NWSArea("PH", "Central Pacific including Hawaii"))
        areaList.add(NWSArea("PS", "South Central Pacific + American Samoa"))
        areaList.add(NWSArea("PM", "Western Pacific + Mariana Island"))
        areaList.add(NWSArea("AN", "Western North Atlantic ME-NC"))
        areaList.add(NWSArea("AM", "Western North Atlantic NC-FL+Caribbean"))
        areaList.add(NWSArea("GM", "Gulf of Mexico"))
        areaList.add(NWSArea("LS", "Lake Superior"))
        areaList.add(NWSArea("LM", "Lake Michigan"))
        areaList.add(NWSArea("LH", "Lake Huron"))
        areaList.add(NWSArea("LC", "Lake St. Clair"))
        areaList.add(NWSArea("LE", "Lake Erie"))
        areaList.add(NWSArea("LO", "Lake Ontario"))
        areaList.add(NWSArea("SL", "St. Lawrence River"))
        areaList.add(NWSArea("marine", "Marine Zones (all)"))
        listener.onResponse(areaList)
    }

    fun getZones(area: NWSArea, listener: Response.Listener<ArrayList<NWSZone>>) {
        val countyList = ArrayList<NWSZone>()
        countyList.add(NWSZone("all", "All"))
        if (area.id == "us-all" || area.id == "marine") {
            listener.onResponse(countyList)
        } else {
            val req = makeRequest("$apiurl/zones/county?area=${area.id}", { response ->
                Log.i("NWSAPI", "Response: $response")
                // TODO: check response for error codes
                val features = response.optJSONArray("features")
                if (features != null) {
                    for (i in 0 until features.length()) {
                        val properties = features.getJSONObject(i).optJSONObject("properties")
                        countyList.add(
                            NWSZone(
                                properties?.optString("id") ?: "",
                                properties?.optString("name") ?: ""
                            )
                        )
                    }
                }
                Log.i("NWSAPI", "Returning: $countyList")
                listener.onResponse(countyList)
            }, { error ->
                Log.i("NWSAPI", "Error: $error")
                listener.onResponse(countyList)
            })
            requestQueue.add(req)
        }
    }

    fun getActiveAlerts(area: NWSArea, zone: NWSZone, listener: Response.Listener<ArrayList<NWSAlert>>) {
        val alertList = ArrayList<NWSAlert>()
        val url = when {
            area.id == "us-all" -> {
                "$apiurl/alerts/active/"
            }
            area.id == "marine" -> {
                "$apiurl/alerts/active?region_type=marine"
            }
            zone.id == "all" -> {
                "$apiurl/alerts/active/area/${area.id}"
            }
            else -> {
                "$apiurl/alerts/active/zone/${zone.id}"
            }
        }
        val req = makeRequest(url, { response ->
            // TODO: check response for error codes
            val features = response.optJSONArray("features")
            if (features != null) {
                for (i in 0 until features.length()) {
                    val alert = features.getJSONObject(i)
                    if (alert != null) {
                        alertList.add(NWSAlert(alert))
                    }
                }
            }
            listener.onResponse(alertList)
        }, { error ->
            Log.i("NWSAPI", "Error: $error")
            listener.onResponse(alertList)
        })
        requestQueue.add(req)
    }
}

class NWSArea(var id: String, var name: String) {

    //to display object as a string in spinner
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other is NWSArea) {
            if (other.name === name && other.id === id) return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

class NWSZone(var id: String, var name: String) {

    //to display object as a string in spinner
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other is NWSZone) {
            if (other.name === name && other.id === id) return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

class NWSAlert(blob: JSONObject) {
    private val properties = blob.getJSONObject("properties")
    val headline: String = properties.optString("headline", "Unknown Alert")
    val description: String = properties.optString("description", "No description provided")

    override fun toString(): String {
        return headline
    }

}