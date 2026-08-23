package com.appvendor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppVendorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize application-level resources here
    }
}
