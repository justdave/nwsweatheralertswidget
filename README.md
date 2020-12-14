NWS Weather Alerts Widget
=========================

Android home screen widget to display current weather alerts from the US National Weather Service

> &#x1F534; **IMPORTANT** &#x1F534;
>
> **The curent code in the repository is a work in progress and is NOT in a complete state. There are many missing features.**
>
> **If you want code that actually works, please use the `1.x-maint` branch (note that you may need an old version of Android Studio to build it)**
>
> See [Rewrite in Kotlin](https://github.com/justdave/nwsweatheralertswidget/projects) for details.

I created this because I wanted a tablet on the wall in my kitchen to display weather alerts on the screen, and for all the plethora of weather apps out there, I couldn't find one that showed anything more than a (!) icon on their widgets for alerts, and you had to click through to find out what they were.  Some of them would put the alerts into the notification bar, but that wasn't much better.  So this one displays a list of the current alerts right on the widget, and that is the only purpose of the widget. If there's more than fits, the list scrolls, and you can tap on an alert to open the full text of the alert in your web browser.

There's also an app with a user interface to go with it, both for picking which state or county to display alerts for, and for debugging.  The app lets you look at the raw XML feed from the NWS, for example.  That might eventually go away now that it's mostly working.

If you feel like helping out or adding cool new features, I welcome pull requests.  Feel free to file issues, too (and check the issue queue for bug reports and feature requests if you're looking for something to do).
