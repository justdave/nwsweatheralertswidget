package net.justdave.nwsweatheralertswidget

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import net.justdave.nwsweatheralertswidget.objects.NWSAlert
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone

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
        return nwsapi.getAreas(listener)
    }

    fun getZonePopupContent(area: NWSArea, listener: Response.Listener<ArrayList<NWSZone>>) {
        return nwsapi.getZones(area, listener)
    }

    fun getDebugContent(
        area: NWSArea,
        zone: NWSZone,
        listener: Response.Listener<List<NWSAlert>>,
        errorListener: Response.ErrorListener
    ) {
        return nwsapi.getActiveAlerts(area, zone, listener, errorListener)
    }

}
