package com.unotangozero.app

import android.app.Application
import com.unotangozero.app.notifications.TaskReminderReceiver
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TangoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskReminderReceiver.ensureChannel(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
