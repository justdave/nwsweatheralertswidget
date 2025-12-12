package net.justdave.nwsweatheralertswidget

import kotlinx.serialization.json.Json

/**
 * A shared, lenient JSON configuration for the entire app.
 * `ignoreUnknownKeys = true` makes the app resilient to new fields being added to the NWS API.
 */
val lenientJson = Json { ignoreUnknownKeys = true }
