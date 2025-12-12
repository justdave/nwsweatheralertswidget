package net.justdave.nwsweatheralertswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.AlertsUpdateService
import net.justdave.nwsweatheralertswidget.NWSAPI
import net.justdave.nwsweatheralertswidget.R
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone

/**
 * The configuration screen for the [AlertsWidget] AppWidget.
 */
class AlertsWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var appWidgetArea: Spinner
    private lateinit var appWidgetZone: Spinner
    private lateinit var nwsapi: NWSAPI

    private var addWidgetClickListener = View.OnClickListener {
        val context = this@AlertsWidgetConfigureActivity

        CoroutineScope(Dispatchers.Main).launch {
            // When the button is clicked, store the string locally
            val area = appWidgetArea.selectedItem as NWSArea
            val zone = appWidgetZone.selectedItem as NWSZone
            val title = if (zone.id != "all" && zone.toString() != "Loading...") {
                zone.toString()
            } else {
                area.toString()
            }
            saveWidgetPrefs(context, appWidgetId, area.id, zone.id, title)

            // Trigger an immediate update of the service
            val serviceIntent = Intent(context, AlertsUpdateService::class.java).apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    private var areaSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (parent.getItemAtPosition(position) == "") {
                appWidgetArea.setSelection(0)
            } else {
                val area = parent.getItemAtPosition(position) as NWSArea
                val loadingMenu = ArrayList<NWSZone>()
                loadingMenu.add(NWSZone("all", "Loading..."))
                appWidgetZone.adapter = ArrayAdapter(
                    applicationContext,
                    R.layout.spinner_layout,
                    loadingMenu
                )
                nwsapi.getZones(area) { response ->
                    Log.i("WidgetConfigure", "ZoneWidget: $response")
                    appWidgetZone.adapter = ArrayAdapter(
                        applicationContext,
                        R.layout.spinner_layout,
                        response
                    )
                    appWidgetZone.setSelection(0)
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        nwsapi = NWSAPI.getInstance(applicationContext)

        setContentView(R.layout.alerts_widget_configure)
        appWidgetArea = findViewById<View>(R.id.appwidget_area) as Spinner
        nwsapi.getAreas { response ->
            Log.i("WidgetConfigure", "AreaWidget: $response")
            appWidgetArea.adapter = ArrayAdapter(
                applicationContext,
                R.layout.spinner_layout,
                response
            )
        }
        appWidgetArea.onItemSelectedListener = areaSelectedListener
        appWidgetZone = findViewById<View>(R.id.appwidget_zone) as Spinner
        nwsapi.getZones(NWSArea("us-all", "")) { response ->
            Log.i("WidgetConfigure", "ZoneWidget: $response")
            appWidgetZone.adapter = ArrayAdapter(
                applicationContext,
                R.layout.spinner_layout,
                response
            )
        }

        val addButton = findViewById<Button>(R.id.add_button)
        addButton.setOnClickListener(addWidgetClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val prefs = loadWidgetPrefs(this@AlertsWidgetConfigureActivity, appWidgetId)
            val areaId = prefs["area"]
            val zoneId = prefs["zone"]
            val title = prefs["title"]

            // If the title is not the default, we are reconfiguring
            if (title != getString(R.string.appwidget_text)) {
                addButton.text = "Save"
            }

            appWidgetArea.setSelection(findSpinnerIndex(appWidgetArea, areaId))
            appWidgetZone.setSelection(findSpinnerIndex(appWidgetZone, zoneId))
        }

    }

    private fun findSpinnerIndex(spinner: Spinner, value: String?): Int {
        for (i in 0 until spinner.count) {
            val item = spinner.getItemAtPosition(i)
            if (item is NWSArea && item.id == value) {
                return i
            }
            if (item is NWSZone && item.id == value) {
                return i
            }
        }
        return 0
    }
}
