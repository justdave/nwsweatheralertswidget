package net.justdave.nwsweatheralertswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
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

        /*
        // When the button is clicked, store the string locally
        val widgetArea = appWidgetArea.toString()
        saveTitlePref(context, appWidgetId, widgetArea)
        */

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId)
        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
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

        findViewById<View>(R.id.add_button).setOnClickListener(addWidgetClickListener)

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

        appWidgetArea.setSelection(0)
        appWidgetZone.setSelection(0)
    }

}

private const val PREFS_NAME = "net.justdave.nwsweatheralertswidget.AlertsWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: context.getString(R.string.appwidget_text)
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}