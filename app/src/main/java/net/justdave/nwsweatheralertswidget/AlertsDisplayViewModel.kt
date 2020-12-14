package net.justdave.nwsweatheralertswidget

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray

class AlertsDisplayViewModel : ViewModel() {

    lateinit var nwsapi: NWSAPI

    fun init() {
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
        return nwsapi.getCounties("MI", {
            response -> listener.onResponse(response.toString())
        })
    }
}
