package net.justdave.nwsweatheralertswidget
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.HtmlCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AboutDialog(context: Context) : Dialog(context) {
    public override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.about)
        val p: PackageInfo?
        var version: String? = "???"
        var vCode: Long = 0
        val tv = findViewById<View>(R.id.info_text) as TextView
        tv.text =readRawTextFile(R.raw.about_info)?.let { HtmlCompat.fromHtml(it,HtmlCompat.FROM_HTML_MODE_LEGACY) }
        tv.setLinkTextColor(Color.BLUE)
        Linkify.addLinks(tv, Linkify.ALL)
        val ver = findViewById<View>(R.id.version_string) as TextView
        val aContext = context.applicationContext
        try {
            p = aContext.packageManager.getPackageInfo(aContext.packageName, 0)
            version = p.versionName
            vCode = PackageInfoCompat.getLongVersionCode(p)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        ver.text = aContext.resources.getString(R.string.about_version, version, vCode)
        val button = findViewById<View>(R.id.about_ok_button) as Button
        button.setOnClickListener { dismiss() }
    }

    private fun readRawTextFile(id: Int): String? {
        val inputStream = context.resources.openRawResource(id)
        val `in` = InputStreamReader(inputStream)
        val buf = BufferedReader(`in`)
        var line: String?
        val text = StringBuilder()
        try {
            while (buf.readLine().also { line = it } != null) text.append(line)
        } catch (e: IOException) {
            return null
        }
        return text.toString()
    }
}