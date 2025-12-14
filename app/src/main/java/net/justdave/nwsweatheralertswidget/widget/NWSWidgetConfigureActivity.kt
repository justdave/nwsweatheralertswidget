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
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.AlertsUpdateService
import net.justdave.nwsweatheralertswidget.NWSAPI
import net.justdave.nwsweatheralertswidget.NWSWidgetProvider
import net.justdave.nwsweatheralertswidget.R
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone
import net.justdave.nwsweatheralertswidget.updateAppWidget

/**
 * The configuration screen for the [NWSWidgetProvider] AppWidget.
 */
class NWSWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var appWidgetArea: Spinner
    private lateinit var appWidgetZone: Spinner
    private lateinit var appWidgetTheme: Spinner
    private lateinit var zoneLoadingSpinner: ProgressBar
    private lateinit var addButton: Button
    private lateinit var nwsapi: NWSAPI

    private var addWidgetClickListener = View.OnClickListener {
        val context = this@NWSWidgetConfigureActivity

        CoroutineScope(Dispatchers.Main).launch {
            // When the button is clicked, store the string locally
            val area = appWidgetArea.selectedItem as NWSArea
            val zone = appWidgetZone.selectedItem as NWSZone
            val theme = appWidgetTheme.selectedItem as String
            val title = if (zone.id != "all" && zone.toString() != getString(R.string.loading)) {
                zone.toString()
            } else {
                area.toString()
            }
            saveWidgetPrefs(context, appWidgetId, area.id, zone.id, title, theme.lowercase())

            // Force the service to restart to pick up configuration changes immediately.
            val serviceIntent = Intent(context, AlertsUpdateService::class.java).apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.stopService(serviceIntent) // Stop it first
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent) // Then start it again
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
            val area = parent.getItemAtPosition(position) as NWSArea
            CoroutineScope(Dispatchers.Main).launch {
                updateZones(area)
                appWidgetZone.setSelection(0)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        nwsapi = NWSAPI.getInstance(this@NWSWidgetConfigureActivity)

        setContentView(R.layout.alerts_widget_configure)
        appWidgetArea = findViewById(R.id.appwidget_area)
        appWidgetZone = findViewById(R.id.appwidget_zone)
        appWidgetTheme = findViewById(R.id.appwidget_theme)
        zoneLoadingSpinner = findViewById(R.id.zone_loading_spinner)
        addButton = findViewById(R.id.add_button)

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
            addButton.isEnabled = false
            val prefs = loadWidgetPrefs(this@NWSWidgetConfigureActivity, appWidgetId)
            val areaId = prefs["area"]
            val zoneId = prefs["zone"]
            val theme = prefs["theme"]
            if (prefs["title"] != getString(R.string.appwidget_text)) {
                addButton.setText(R.string.save)
            }

            // Set up the area spinner
            val areas = nwsapi.getAreas()
            appWidgetArea.adapter = ArrayAdapter(this@NWSWidgetConfigureActivity, R.layout.spinner_layout, areas)
            val areaIndex = findSpinnerIndex(appWidgetArea, areaId)

            // Disable the listener, set the selection, then fetch the zones for that area
            appWidgetArea.onItemSelectedListener = null
            appWidgetArea.setSelection(areaIndex)
            val selectedArea = appWidgetArea.getItemAtPosition(areaIndex) as NWSArea
            updateZones(selectedArea)

            // Now that zones are populated, find the stored zone and select it
            val zoneIndex = findSpinnerIndex(appWidgetZone, zoneId)
            appWidgetZone.setSelection(zoneIndex)

            // Set up the theme spinner
            val themes = resources.getStringArray(R.array.widget_themes)
            appWidgetTheme.adapter = ArrayAdapter(this@NWSWidgetConfigureActivity, R.layout.spinner_layout, themes)
            val themeIndex = themes.indexOf(theme?.replaceFirstChar { it.uppercase() })
            appWidgetTheme.setSelection(if (themeIndex != -1) themeIndex else 0)

            // Re-enable the listener for user interaction
            appWidgetArea.onItemSelectedListener = areaSelectedListener
            addButton.isEnabled = true
        }

    }

    private suspend fun updateZones(area: NWSArea) {
        zoneLoadingSpinner.visibility = View.VISIBLE
        appWidgetZone.isEnabled = false
        addButton.isEnabled = false
        val loadingMenu = arrayListOf(NWSZone("all", getString(R.string.loading)))
        appWidgetZone.adapter = ArrayAdapter(
            this@NWSWidgetConfigureActivity,
            R.layout.spinner_layout,
            loadingMenu
        )

        // Fetch the new list of zones
        val zones = nwsapi.getZones(area)
        Log.i("WidgetConfigure", "Zones: $zones")
        appWidgetZone.adapter = ArrayAdapter(
            this@NWSWidgetConfigureActivity,
            R.layout.spinner_layout,
            zones
        )
        // Hide the loading indicator
        zoneLoadingSpinner.visibility = View.GONE
        appWidgetZone.isEnabled = true
        addButton.isEnabled = true
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
