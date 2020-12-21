package net.justdave.nwsweatheralertswidget

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

private const val apiurl = "https://api.weather.gov"
// The documentation for this API is at https://www.weather.gov/documentation/services-web-api

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

    private fun makeRequest(url: String, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener): JsonObjectRequest {
        return object: JsonObjectRequest(Method.GET, url, null,
            listener,
            errorListener){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = "(http://justdave.github.io/nwsweatheralertswidget, playstoresupport@justdave.net)"
                    return headers
            }
        }
    }

    fun getCounties(state: String, listener: Response.Listener<JSONArray>) {
        val req = makeRequest("$apiurl/zones/county?area=$state", {
                response ->
            Log.i("NWSAPI", "Response: $response")
            listener.onResponse(response.optJSONArray("features"))
        }, {
                error ->
            Log.i("NWSAPI", "Error: $error")
            listener.onResponse(JSONArray())
        })
        requestQueue.add(req)
    }

}