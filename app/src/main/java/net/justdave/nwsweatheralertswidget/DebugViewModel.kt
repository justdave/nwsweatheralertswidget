package net.justdave.nwsweatheralertswidget

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response

class DebugViewModel : ViewModel() {
    private lateinit var nwsapi: NWSAPI

    init {
        Log.i("DebugViewModel","Created!")
    }

    fun initializeContext(context: Context) {
        nwsapi = NWSAPI.getInstance(context)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("DebugViewModel", "destroyed!")
    }

    fun getAreaPopupContent(listener: Response.Listener<ArrayList<NWSArea>>) {
        return nwsapi.getAreas { response ->
            listener.onResponse(response)
        }
    }

    fun getCountyPopupContent(area: NWSArea, listener: Response.Listener<ArrayList<NWSCounty>>) {
        return nwsapi.getCounties(area) { response ->
            listener.onResponse(response)
        }
    }

    fun getDebugContent(area: NWSArea, zone: NWSCounty, listener: Response.Listener<ArrayList<NWSAlert>>) {
        return nwsapi.getActiveAlerts(area, zone) { response ->
            listener.onResponse(response)
        }
    }

}