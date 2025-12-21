package net.justdave.nwsweatheralertswidget

import android.app.AlertDialog
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import androidx.navigation.NavController
import io.noties.markwon.Markwon

fun showAboutDialog(context: Context, navController: NavController) {
    val builder = AlertDialog.Builder(context)
    val inflater = LayoutInflater.from(context)
    val view = inflater.inflate(R.layout.about, null)

    val versionView = view.findViewById<TextView>(R.id.version_string)
    val aboutView = view.findViewById<TextView>(R.id.info_text)

    // Set version string
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    versionView.text = context.getString(R.string.about_version, info.versionName, PackageInfoCompat.getLongVersionCode(info))

    // Set about text and make links clickable
    val markdown = context.assets.open("ABOUT.md").bufferedReader().use { it.readText() }
    val markwon = Markwon.create(context)
    markwon.setMarkdown(aboutView, markdown)
    aboutView.movementMethod = LinkMovementMethod.getInstance()

    builder.setTitle(R.string.action_about)
    builder.setView(view)

    builder.setPositiveButton(R.string.ok) { dialog, _ ->
        dialog.dismiss()
    }

    builder.setNeutralButton("Privacy Policy") { dialog, _ ->
        navController.navigate(R.id.privacyPolicyFragment)
        dialog.dismiss()
    }

    builder.setNegativeButton("Changelog") { dialog, _ ->
        navController.navigate(R.id.changelogFragment)
        dialog.dismiss()
    }

    builder.create().show()
}
