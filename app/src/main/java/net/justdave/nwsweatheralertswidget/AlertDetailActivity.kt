package net.justdave.nwsweatheralertswidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbarIcon: ImageView
    private lateinit var expires: TextView
    private lateinit var description: TextView
    private lateinit var instructions: TextView
    private lateinit var descriptionHeader: TextView
    private lateinit var instructionsHeader: TextView
    private lateinit var target: TextView
    private lateinit var rawData: TextView
    private lateinit var rawScroller: ScrollView
    private lateinit var rawDivider: android.view.View

    private lateinit var paramsCard: android.view.View
    private lateinit var windGust: TextView
    private lateinit var hailSize: TextView
    private lateinit var thunderstormDamage: TextView
    private lateinit var tornadoDetection: TextView
    private lateinit var hailThreat: TextView
    private lateinit var windThreat: TextView

    private lateinit var layoutWindGust: android.view.View
    private lateinit var layoutHailSize: android.view.View
    private lateinit var layoutThunderstormDamage: android.view.View
    private lateinit var layoutTornadoDetection: android.view.View
    private lateinit var layoutHailThreat: android.view.View
    private lateinit var layoutWindThreat: android.view.View
    private lateinit var groupWind: android.view.View
    private lateinit var groupHail: android.view.View

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertdetail)

        toolbar = findViewById(R.id.detail_toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        toolbarIcon = findViewById(R.id.toolbar_icon)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Using our custom TextView

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        expires = findViewById(R.id.detail_expires)
        description = findViewById(R.id.detail_description)
        instructions = findViewById(R.id.detail_instructions)
        descriptionHeader = findViewById(R.id.detail_description_header)
        instructionsHeader = findViewById(R.id.detail_instruction_header)
        target = findViewById(R.id.detail_target)
        rawData = findViewById(R.id.event_raw_json)
        rawScroller = findViewById(R.id.event_raw_scroller)
        rawDivider = findViewById(R.id.detail_raw_divider)

        paramsCard = findViewById(R.id.detail_params_card)
        windGust = findViewById(R.id.detail_wind_gust)
        hailSize = findViewById(R.id.detail_hail_size)
        thunderstormDamage = findViewById(R.id.detail_thunderstorm_damage)
        tornadoDetection = findViewById(R.id.detail_tornado_detection)
        hailThreat = findViewById(R.id.detail_hail_threat)
        windThreat = findViewById(R.id.detail_wind_threat)

        layoutWindGust = findViewById(R.id.layout_wind_gust)
        layoutHailSize = findViewById(R.id.layout_hail_size)
        layoutThunderstormDamage = findViewById(R.id.layout_thunderstorm_damage)
        layoutTornadoDetection = findViewById(R.id.layout_tornado_detection)
        layoutHailThreat = findViewById(R.id.layout_hail_threat)
        layoutWindThreat = findViewById(R.id.layout_wind_threat)
        groupWind = findViewById(R.id.group_wind)
        groupHail = findViewById(R.id.group_hail)

        if (savedInstanceState != null) {
            val showRawJson = savedInstanceState.getBoolean("show_raw_json", false)
            rawScroller.isVisible = showRawJson
            rawDivider.isVisible = showRawJson
        }

        loadDataFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadDataFromIntent(intent)
    }

    private fun loadDataFromIntent(intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val alertId = intent.getStringExtra("alert_id")
        val directAlert: NWSAlert? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("alert", NWSAlert::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("alert")
        }

        Log.i(TAG, "Widget ID: $appWidgetId")
        Log.i(TAG, "Alert ID: $alertId")

        if (directAlert != null) {
            updateUi(directAlert)
        } else if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && alertId != null) {
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
        toolbarTitle.text = data.event
        toolbarIcon.setImageResource(data.getIcon())

        val backgroundColorRes = data.getBackgroundColor()
        toolbar.setBackgroundResource(backgroundColorRes)

        // Adjust text color based on background
        // Red and Blue get white text, others (Yellow, Orange, Grey) get black text
        if (backgroundColorRes == R.color.nws_red || backgroundColorRes == R.color.nws_blue) {
            toolbarTitle.setTextColor(android.graphics.Color.WHITE)
            toolbar.navigationIcon?.setTint(android.graphics.Color.WHITE)
            toolbar.overflowIcon?.setTint(android.graphics.Color.WHITE)
        } else {
            toolbarTitle.setTextColor(android.graphics.Color.BLACK)
            toolbar.navigationIcon?.setTint(android.graphics.Color.BLACK)
            toolbar.overflowIcon?.setTint(android.graphics.Color.BLACK)
        }

        val smartDesc = data.getSmartDescription()
        description.text = smartDesc
        description.isVisible = smartDesc.isNotEmpty()
        descriptionHeader.isVisible = smartDesc.isNotEmpty()

        val smartInstr = data.getSmartInstruction()
        instructions.text = smartInstr
        instructions.isVisible = smartInstr.isNotEmpty()
        instructionsHeader.isVisible = smartInstr.isNotEmpty()

        target.text = getString(R.string.detail_target, data.areaDesc)

        val expiresString = data.expires
        if (expiresString.isNotEmpty()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                val eventExpires = format.parse(expiresString)
                val displayFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG)
                expires.text = getString(R.string.detail_expires, displayFormat.format(eventExpires!!))
            } catch (e: ParseException) {
                Log.w(TAG, "Could not parse expires date: $expiresString", e)
                expires.text = getString(R.string.detail_unparseable_date)
            }
        } else {
            expires.text = getString(R.string.detail_no_expiration)
        }

        var hasParams = false
        if (data.maxWindGust.isNotEmpty()) {
            windGust.text = if (data.maxWindGust.contains("MPH", ignoreCase = true)) {
                data.maxWindGust
            } else {
                getString(R.string.unit_mph, data.maxWindGust)
            }
            layoutWindGust.isVisible = true
            groupWind.isVisible = true
            hasParams = true
            val windSpeed = data.maxWindGust.filter { it.isDigit() }.toIntOrNull() ?: 0
            if (windSpeed >= 70) {
                windGust.setTextColor(android.graphics.Color.RED)
            }
        }
        if (data.maxHailSize.isNotEmpty()) {
            hailSize.text = if (data.maxHailSize.contains("in", ignoreCase = true)) {
                data.maxHailSize
            } else {
                getString(R.string.unit_inches, data.maxHailSize)
            }
            layoutHailSize.isVisible = true
            groupHail.isVisible = true
            hasParams = true
            val hailSizeVal = data.maxHailSize.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
            if (hailSizeVal >= 1.5) {
                hailSize.setTextColor(android.graphics.Color.RED)
            }
        }
        if (data.thunderstormDamageThreat.isNotEmpty()) {
            thunderstormDamage.text = data.thunderstormDamageThreat
            layoutThunderstormDamage.isVisible = true
            hasParams = true
            if (data.thunderstormDamageThreat.contains("DESTRUCTIVE", ignoreCase = true)) {
                thunderstormDamage.setTextColor(android.graphics.Color.RED)
            } else if (data.thunderstormDamageThreat.contains("CONSIDERABLE", ignoreCase = true)) {
                thunderstormDamage.setTextColor("#FFA500".toColorInt()) // Orange
            }
        }
        if (data.tornadoDetection.isNotEmpty()) {
            tornadoDetection.text = data.tornadoDetection
            layoutTornadoDetection.isVisible = true
            hasParams = true
            if (data.tornadoDetection.contains("OBSERVED", ignoreCase = true)) {
                tornadoDetection.setTextColor(android.graphics.Color.RED)
            }
        }
        if (data.hailThreat.isNotEmpty()) {
            hailThreat.text = data.hailThreat
            layoutHailThreat.isVisible = true
            groupHail.isVisible = true
            hasParams = true
        }
        if (data.windThreat.isNotEmpty()) {
            windThreat.text = data.windThreat
            layoutWindThreat.isVisible = true
            groupWind.isVisible = true
            hasParams = true
        }
        paramsCard.isVisible = hasParams

        rawData.text = data.getRawDataForDisplay()
        Log.i(TAG, "Activity Updated.")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val showJson = menu.findItem(R.id.detail_action_showjson)
        val hideJson = menu.findItem(R.id.detail_action_hidejson)
        showJson.isVisible = rawScroller.isGone
        hideJson.isVisible = rawScroller.isVisible
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.detail_action_showjson -> {
                rawScroller.isVisible = true
                rawDivider.isVisible = true
                invalidateOptionsMenu()
                return true
            }
            R.id.detail_action_hidejson -> {
                rawScroller.isVisible = false
                rawDivider.isVisible = false
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("show_raw_json", rawScroller.isVisible)
    }

    companion object {
        private const val TAG = "AlertDetailActivity"
    }
}
