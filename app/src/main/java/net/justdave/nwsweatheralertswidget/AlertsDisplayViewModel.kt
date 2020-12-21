package net.justdave.nwsweatheralertswidget

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response

class AlertsDisplayViewModel : ViewModel() {

    private lateinit var nwsapi: NWSAPI

    init {
        Log.i("MainViewModel","MainViewModel Created!")

        /*       viewModelScope.launch(Dispatchers.IO) {
                   val emptyView: TextView = context.contentResolver.findViewById<TextView>(R.id.emptytext)
                   emptyView.setText(nwsapi.getCounties("MI").toString())
               } */
    }
    fun initializeRequestQueue(context: Context) {
        nwsapi = NWSAPI.getInstance(context)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "MainViewModel destroyed!")
    }


    fun getEmptyText(listener: Response.Listener<String>) {
        return nwsapi.getCounties("MI") { response ->
            listener.onResponse(response.toString())
        }
    }
}
