package com.showerly.app

import android.app.Application
import com.showerly.app.di.AppContainer

class ShowerlyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
