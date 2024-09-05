package two.helix.smartspacer.usagedirect.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.widget.TextView
import androidx.window.layout.WindowMetricsCalculator
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject
import two.helix.smartspacer.usagedirect.UsageData

/**
 * Returns [AppWidgetProviderInfo] given [packageName] and [className] of that provider.
 * Taken from https://github.com/pacjo/SmartspacerPlugins/blob/main/app/src/main/java/nodomain/pacjo/smartspacer/plugin/utils/PackageManager.kt#L39-L48
 */
fun getProvider(context: Context, packageName: String, className: String): AppWidgetProviderInfo? {
    val appWidgetManager = AppWidgetManager.getInstance(context)

    return appWidgetManager.installedProviders.firstOrNull {
        it.provider.packageName == packageName && it.provider.className == className
    }
}

class UsageDirectProvider: SmartspacerWidgetProvider() {
    companion object {
        const val PACKAGE_NAME = "godau.fynn.usagedirect"
        private const val PROVIDER_CLASS = "godau.fynn.usagedirect.widget.TimeTodayWidgetProvider"
        private const val ID_TEXT = "godau.fynn.usagedirect:id/widget_text"
    }

    private val usageData by inject<UsageData>()

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        //Load the RemoteViews into regular Views
        //Log.d(UsageDirectProvider)
        val views = remoteViews?.load() ?: return
        val textView = views.findViewByIdentifier<TextView>(ID_TEXT) ?: return

        if (textView != null) {
            val text = textView.text.toString()

            Log.d("UsageDirectProvider", "Got text from widget: $text")
            usageData.setRawTime(text)
        } else {
            Log.e("UsageDirectProvider", "Failed to load text from widget!")
        }
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext(), PACKAGE_NAME, PROVIDER_CLASS)
    }

    override fun getConfig(smartspacerId: String): Config {
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(provideContext())

        // Do the math to make sure that usageDirect shows the full time and not less because of the widget dimensions
        // https://codeberg.org/fynngodau/usageDirect/src/branch/main/Application/src/database/java/godau/fynn/usagedirect/widget/TimeTodayWidgetProvider.java#L28-L47
        val fixedRatio = 10
        val fixedHeightDp = 94
        val wantedWidthDp = ((fixedHeightDp - 44) / 2) * fixedRatio

        // https://developer.android.com/reference/kotlin/android/view/WindowMetrics
        val wantedWidthPx = windowMetrics.density * wantedWidthDp
        val wantedHeightPx = windowMetrics.density * fixedHeightDp

        Log.d("UsageDirectProvider", "Width in dp/px: $wantedWidthDp/$wantedWidthPx\nHeight in dp/px: $fixedHeightDp/$wantedHeightPx")

        return Config(
            width = wantedWidthPx.toInt(),
            height = wantedHeightPx.toInt()
        )
    }
}