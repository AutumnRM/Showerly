package com.showerly.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.showerly.app.di.AppContainer
import com.showerly.app.ui.home.HomeScreen
import com.showerly.app.ui.settings.SettingsScreen

@Composable
fun AppRoot(container: AppContainer) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> HomeScreen(container)
                else -> SettingsScreen(container)
            }
        }
        NavigationBar {
            NavigationBarItem(
                selected = tab == 0,
                onClick = { tab = 0 },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text("主页") }
            )
            NavigationBarItem(
                selected = tab == 1,
                onClick = { tab = 1 },
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                label = { Text("设置") }
            )
        }
    }
}
