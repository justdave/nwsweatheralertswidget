package net.justdave.nwsweatheralertswidget

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {

    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setLogo(R.mipmap.app_icon)

        // As a developer convenience, start the service if it isn't already running.
        if (!AlertsUpdateService.isRunning) {
            val serviceIntent = Intent(this, AlertsUpdateService::class.java).apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onPostCreate(
        savedInstanceState: Bundle?
    ) {
        super.onPostCreate(savedInstanceState)
        navController = this.findNavController(R.id.my_nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        //NavigationUI.setupWithNavController(navigationView, navController)
    }
}
