package net.justdave.nwsweatheralertswidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.objects.NWSAlert
import net.justdave.nwsweatheralertswidget.widget.loadAlerts
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class AlertDetailActivity : AppCompatActivity() {

    private lateinit var event: TextView
    private lateinit var expires: TextView
    private lateinit var description: TextView
    private lateinit var instructions: TextView
    private lateinit var target: TextView
    private lateinit var rawData: TextView
    private lateinit var scroller: ScrollView
    private lateinit var rawScroller: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertdetail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        event = findViewById(R.id.detail_event)
        expires = findViewById(R.id.detail_expires)
        description = findViewById(R.id.detail_description)
        instructions = findViewById(R.id.detail_instructions)
        target = findViewById(R.id.detail_target)
        rawData = findViewById(R.id.event_raw_xml)
        scroller = findViewById(R.id.detail_main_scroller)
        rawScroller = findViewById(R.id.event_raw_scroller)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val alertId = intent.getStringExtra("alert_id")
        Log.i(TAG, "Widget ID: $appWidgetId")
        Log.i(TAG, "Alert ID: $alertId")

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && alertId != null) {
            lifecycleScope.launch {
                val serializedAlerts = loadAlerts(this@AlertDetailActivity, appWidgetId)
                val alerts = lenientJson.decodeFromString<List<NWSAlert>>(serializedAlerts)
                val data = alerts.find { it.id == alertId }
                if (data != null) {
                    updateUi(data)
                } else {
                    Log.e(TAG, "Alert with ID $alertId not found in widget $appWidgetId data, finishing activity.")
                    finish()
                }
            }
        } else {
            Log.e(TAG, "Alert ID or Widget ID not found in intent extras, finishing activity.")
            finish()
        }
    }

    private fun updateUi(data: NWSAlert) {
        scroller.isVisible = true
        event.text = data.event
        description.text = data.description
        instructions.text = data.instruction
        target.text = data.areaDesc

        val expiresString = data.expires
        if (expiresString.isNotEmpty()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                val eventExpires = format.parse(expiresString)
                val displayFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG)
                expires.text = displayFormat.format(eventExpires!!)
            } catch (e: ParseException) {
                Log.w(TAG, "Could not parse expires date: $expiresString", e)
                expires.text = getString(R.string.detail_unparseable_date)
            }
        } else {
            expires.text = getString(R.string.detail_no_expiration)
        }

        rawData.text = data.getRawDataForDisplay()
        val image = findViewById<ImageView>(R.id.detail_icon)
        image.setImageResource(data.getIcon())
        Log.i(TAG, "Activity Updated.")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val showXml = menu.findItem(R.id.detail_action_showxml)
        val hideXml = menu.findItem(R.id.detail_action_hidexml)
        showXml.isVisible = rawScroller.isGone
        hideXml.isVisible = rawScroller.isVisible
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.detail_action_showxml -> {
                rawScroller.isVisible = true
                invalidateOptionsMenu()
                return true
            }
            R.id.detail_action_hidexml -> {
                rawScroller.isVisible = false
                invalidateOptionsMenu()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "AlertDetailActivity"
    }
}
