package net.justdave.nwsweatheralertswidget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller


class MainActivity : AppCompatActivity() {

    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upgradeSecurityProvider()
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setLogo(R.mipmap.app_icon)
    }

    private fun upgradeSecurityProvider() {
        ProviderInstaller.installIfNeededAsync(this, object :
            ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstalled() {
                // We don't actually need to do anything here but the implementation requires we override this.
            }

            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
                GoogleApiAvailability.getInstance()
                    .showErrorNotification(this@MainActivity, errorCode)
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onPostCreate(
        savedInstanceState: Bundle?
    ) {
        super.onPostCreate(savedInstanceState)
        navController = Navigation.findNavController(this, R.id.my_nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        //NavigationUI.setupWithNavController(navigationView, navController)
    }
}

