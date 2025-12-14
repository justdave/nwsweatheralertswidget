# NWS Weather Alerts Widget Change History

## unreleased changes

* More dark mode readability fixes.
* Text in the Alert Detail screen can now be selected and copied.

## Version 2.1 (Dec 14, 2025)

* About dialog, widget configuration screen, and debug screen are now readable in Dark mode.
* Update interval is now 5 minutes instead of 15 (just like in version 1.x)
* Back button behavior on Android 12 from a widget-launched detail screen is now correct.
* Restores the semitransparent theme from version 1.x. But gives you the option per-widget to switch to the light and dark themes if you want them.
* On versions of Android that are too old to have the built-in widget reconfiguration feature, there is now a wrench icon in the corner of the widget that lets you reconfigure it.

## Version 2.0 (Dec 13, 2025)

* Almost ground up rewrite in Kotlin
* Target modern Android versions
* Re-skin UI in Material Design 3
* Use NWS's new REST API
* Can now have multiple widgets each with their own location, and widgets are reconfigurable.
* Last update date is now shown on the widget
* in-app screens now look better in landscape mode

## Version 1.2.1-fdroid1 (Dec 3, 2025)

This is the same as version 1.2.1 except has the unused Google Play Services libraries removed from the dependencies, so that FDroid can build it. Since it's otherwise the same under the hood I won't upload it to the Play Store (if you're getting it from the Play Store you already have that stuff on your system anyway).

## Version 1.2.1 (Dec 2, 2025)

* Add notice of deprecated NWS server and how to follow updates for upcoming update that works with the new NWS system

## Version 1.2 (Dec 2, 2025)

* Now requires Android 7.0 or later (required for TLS connections to the NWS server)
* LetsEncrypt root certificate is now embedded to allow Android < 8 to connect to the NWS
* Targets Android 16

## Version 1.1.4 (Dec 22, 2020)

* Built against target API 30 (minimum API 14 still)
* Fix "waiting for feed download" after Dec 11, 2020 NWS requirements changes
* several crash fixes

Version 2.0 is coming soon!

## Version 1.1.3 (Jun 18, 2016)

* NWS started requiring HTTPS to retrieve alert feeds. The app has been updated accordingly. Should fix the "Waiting for feed download" that never goes away.

NOTE: This problem has resurfaced on or around December 11, 2020, and is NOT what got fixed in this release. Version 1.x is EOL because I have to rewrite half of it to comply with new Google Play policies before they'll let me upload a new version. Expect a 2.0 release soon that will fix this again.

## Version 1.1.2 (Jun 8, 2016)

* fixes a crash when attempting to retrieve an expired alert

## Version 1.1.1 (May 23, 2016)

* The main app now tells you when there are no alerts to display instead of just showing a blank white screen

## Version 1.1 (Nov 26, 2014)

* Alert detail text is now rendered in the app in a format suitable for the device rather than loading the NWS site in your web browser when you tap on an alert.
* We no longer show a dummy alert box in the widget when there are no active alerts, making it easier to tell from a distance if there's a real alert or not.
* Added "tsunami" as a keyword for the wave icon.
* Added "freeze" and "frost" as keywords for the icicle icon.

## Version 1.0.5 (Nov 19, 2014)

* fix failure of widget to refresh on its own in Jelly Bean and Kit Kat.

## Version 1.0.4 (Mar 5, 2014)

* fix a crash at startup that only happens on Android 4.0.x

## Version 1.0.3 (Mar 4, 2014)

Bugfixes:

* Fixed a bug that was causing the widget to only display "Widget Loading" for long periods of time on slower devices.

Features:

* Added a "Demo all alert types" function in the app, which shows a list of every alert type the NWS knows how to publish. This is mostly for development purposes, to easily tell which items have icons and colors assigned yet and which don't. You can help by offering suggestions for what kinds of icons to use in Issue #8 .

## Version 1.0.2 (Mar 1, 2014)

* Added "Freezing" as a trigger word for the "Ice" icon.
* Added "Rain", "Marine", and "Surf" as additional trigger words for a blue background.
* Added "Fire" icon to match events with "Fire" and "Red Flag" in the name.
* Added "Wave" icon for events with "Surf" in the name.
* Added an orange background for events with "Fire" and "Dust" in the name
* Redid the About box to use the version number from the manifest file so I only have to change it in one place when I release in the future.

## Version 1.0.1 (Mar 1, 2014)

minor update to fix the about box, had the wrong icon and website URL in it.

## Version 1.0 (Mar 1, 2014)

Initial release
