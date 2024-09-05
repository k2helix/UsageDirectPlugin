package two.helix.smartspacer.usagedirect

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class UsageApp : Application() {
    private val singles = module {
        single<UsageData> { UsageDataImpl(get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@UsageApp)
            modules(singles)
        }
    }
}