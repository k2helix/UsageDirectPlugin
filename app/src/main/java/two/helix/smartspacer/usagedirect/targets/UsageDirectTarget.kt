package two.helix.smartspacer.usagedirect.targets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import two.helix.smartspacer.usagedirect.BuildConfig
import two.helix.smartspacer.usagedirect.R
import two.helix.smartspacer.usagedirect.UsageData
import two.helix.smartspacer.usagedirect.providers.UsageDirectProvider
import android.graphics.drawable.Icon as AndroidIcon

class UsageDirectTarget: SmartspacerTargetProvider() {
    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.target.usage"
    }

    private val usageData by inject<UsageData>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        usageData.addSmartspacerIdIfNeeded(smartspacerId)
        val data = usageData.getData() ?: return emptyList()

        if (data.formattedTime == "") return emptyList()

        return listOf(
            TargetTemplate.Basic(
                "usage_direct_$smartspacerId",
                ComponentName(provideContext(), this::class.java),
                title = Text("${resources.getString(R.string.target_usage_direct_screen_time)}: ${data.formattedTime}"),
                subtitle = Text(resources.getString(R.string.target_usage_direct_label)),
                icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_usage_direct)),
                onClick = TapAction(intent = data.clickIntent)
            ).create().also { it.canBeDismissed = false }
        )
    }

    override fun getConfig(smartspacerId: String?): Config {
        val appWidgetManager = AppWidgetManager.getInstance(provideContext())

        val compatible: CompatibilityState = if (appWidgetManager.getInstalledProvidersForPackage(UsageDirectProvider.PACKAGE_NAME, null).isEmpty()) {
            val unsupported = provideContext().getString(R.string.target_usage_direct_missing)
            CompatibilityState.Incompatible(unsupported)
        } else {
            CompatibilityState.Compatible
        }

        return Config(
            label = resources.getString(R.string.target_usage_direct_label),
            description = resources.getString(R.string.target_usage_direct_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_usage_direct),
            compatibilityState = compatible,
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widget.usage",
            refreshPeriodMinutes = 1,
            allowAddingMoreThanOnce = true
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String) = false

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        usageData.removeSmartspacerId(smartspacerId)
    }
}
