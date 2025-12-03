package net.justdave.nwsweatheralertswidget

import net.justdave.nwsweatheralertswidget.objects.NWSAlert
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NWSAlertTest {
    @Test
    fun parsing_isCorrect() {
        val fakeAlert =
            """{ "properties": {
                "headline": "Fake Headline",
                "description": "Fake Description"
            }}"""
        val alert = NWSAlert(JSONObject(fakeAlert))
        assertEquals("Fake Headline", alert.toString())
    }
}
