package two.helix.smartspacer.usagedirect

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat.startActivity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import two.helix.smartspacer.usagedirect.complications.UsageDirectComplication
import two.helix.smartspacer.usagedirect.providers.UsageDirectProvider
import two.helix.smartspacer.usagedirect.targets.UsageDirectTarget


interface UsageData {

    fun addSmartspacerIdIfNeeded(id: String)
    fun removeSmartspacerId(id: String)
    fun getData(): UsageDataState?
    fun setData(usageDataState: UsageDataState)
    fun setRawTime(time: CharSequence)
    fun refreshWidgetIfNeeded(id: String)

    data class UsageDataState(
        var rawTime: CharSequence,
        var formattedTime: CharSequence,
        var hours: Int,
        var minutes: Int,
        var seconds: Int,
        val clickIntent: Intent?
    )

}

class UsageDataImpl(private val context: Context): UsageData {
    private var data: UsageData.UsageDataState? = null

    /**
     *  Local in-memory set of Target & Complication ids. This is used to know whether to trigger a
     *  refresh when Smartspace becomes visible.
     */
    @VisibleForTesting
    val ids = HashSet<String>()

    override fun getData(): UsageData.UsageDataState? {
        return data
    }

    override fun setData(usageDataState: UsageData.UsageDataState) {
        data = usageDataState
        SmartspacerTargetProvider.notifyChange(context, UsageDirectTarget::class.java)
        SmartspacerComplicationProvider.notifyChange(
            context, UsageDirectComplication::class.java
        )
    }

    override fun setRawTime(time: CharSequence) {
        val parts = time.split(":")
        val hours: Int
        val minutes: Int
        val seconds: Int

        var formattedTime = ""

        when (parts.size) {
            3 -> {
                hours = parts[0].toInt()
                minutes = parts[1].toInt()
                seconds = parts[2].toInt()
                formattedTime += "${hours}${context.resources.getString(R.string.short_for_hours)} " +
                                "${minutes}${context.resources.getString(R.string.short_for_minutes)}"
            }
            2 -> {
                hours = parts[0].toInt()
                minutes = parts[1].toInt()
                seconds = 0
                formattedTime += "${hours}${context.resources.getString(R.string.short_for_hours)} " +
                                "${minutes}${context.resources.getString(R.string.short_for_minutes)}"
            }
            1 -> {
                hours = parts[0].toInt()
                minutes = 0
                seconds = 0
                formattedTime = "${hours}${context.resources.getString(R.string.short_for_hours)}"
            }
            else -> {
                hours = 0
                minutes = 0
                seconds = 0
            }
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(UsageDirectProvider.PACKAGE_NAME)

        val localData = UsageData.UsageDataState(
            time,
            formattedTime,
            hours,
            minutes,
            seconds,
            launchIntent
        )
        setData(localData)
    }

    override fun addSmartspacerIdIfNeeded(id: String) {
        if(ids.add(id)){
            refreshWidgetIfNeeded(id)
        }
    }

    override fun removeSmartspacerId(id: String) {
        ids.remove(id)
    }

    override fun refreshWidgetIfNeeded(id: String) {
        if(!ids.contains(id)) return
        SmartspacerWidgetProvider.clickView(
            context, id, "godau.fynn.usagedirect:id/widget_layout"
        )
    }
}