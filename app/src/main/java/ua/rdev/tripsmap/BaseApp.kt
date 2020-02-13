package ua.rdev.tripsmap

import android.app.Application
import org.koin.core.context.startKoin

class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(mainModule)
        }
    }
}