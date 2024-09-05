package two.helix.smartspacer.usagedirect.complications

import android.appwidget.AppWidgetManager
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import two.helix.smartspacer.usagedirect.BuildConfig
import two.helix.smartspacer.usagedirect.R
import two.helix.smartspacer.usagedirect.UsageData
import two.helix.smartspacer.usagedirect.providers.UsageDirectProvider
import android.graphics.drawable.Icon as AndroidIcon

class UsageDirectComplication: SmartspacerComplicationProvider() {
    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.complication.usage"
    }

    private val usageData by inject<UsageData>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        usageData.addSmartspacerIdIfNeeded(smartspacerId)
        val data = usageData.getData() ?: return emptyList()

        if (data.formattedTime == "") return emptyList()

        return listOf(
            ComplicationTemplate.Basic(
                "usage_direct_$smartspacerId",
                Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_usage_direct)),
                Text(data.formattedTime),
                TapAction(intent = data.clickIntent)
            ).create().apply {
                extras.putBoolean(SmartspaceAction.KEY_EXTRA_HIDE_TITLE_ON_AOD, true)
            }
        )

    }

    override fun getConfig(smartspacerId: String?): Config {
        val appWidgetManager = AppWidgetManager.getInstance(provideContext())

        val compatible: CompatibilityState = if (appWidgetManager.getInstalledProvidersForPackage(UsageDirectProvider.PACKAGE_NAME, null).isEmpty()) {
            val unsupported = provideContext().getString(R.string.complication_usage_direct_missing)
            CompatibilityState.Incompatible(unsupported)
        } else {
            CompatibilityState.Compatible
        }

        return Config(
            label = resources.getString(R.string.complication_usage_direct_label),
            description = resources.getString(R.string.complication_usage_direct_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_usage_direct),
            compatibilityState = compatible,
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widget.usage",
            refreshPeriodMinutes = 1,
            allowAddingMoreThanOnce = true
        )
    }

    override fun onProviderRemoved(smartspacerId: String) {
        usageData.removeSmartspacerId(smartspacerId)
    }
}