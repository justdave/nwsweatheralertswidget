package net.justdave.nwsweatheralertswidget

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel

class AlertsDisplayViewModel : ViewModel() {

    private lateinit var nwsapi: NWSAPI

    init {
        Log.i("AlertsDisplayViewModel","Created!")
    }

    fun initializeContext(context: Context) {
        nwsapi = NWSAPI.getInstance(context)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("AlertsDisplayViewModel", "destroyed!")
    }

}
