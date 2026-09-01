package com.showerly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.showerly.app.ui.AppRoot
import com.showerly.app.ui.theme.ShowerlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ShowerlyApplication).container
        setContent {
            ShowerlyTheme {
                AppRoot(container)
            }
        }
    }
}
