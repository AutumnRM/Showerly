package com.showerly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.showerly.app.data.settings.AppSettings
import com.showerly.app.ui.AppRoot
import com.showerly.app.ui.theme.ShowerlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ShowerlyApplication).container
        setContent {
            val settings by container.settingsRepository.settings.collectAsState(initial = AppSettings())
            ShowerlyTheme(
                darkPref = settings.darkModeEnum
            ) {
                AppRoot(container)
            }
        }
    }
}
