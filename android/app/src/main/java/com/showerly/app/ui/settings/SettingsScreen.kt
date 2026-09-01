package com.showerly.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.showerly.app.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val state by vm.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("校方接口地址", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = state.endpoint,
                onValueChange = vm::setEndpoint,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://.../api/crowd") },
                singleLine = true
            )

            Text("认证头名称", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = state.headerName,
                onValueChange = vm::setHeaderName,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Authorization") },
                singleLine = true
            )

            Text("认证头值（含 token）", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = state.headerValue,
                onValueChange = vm::setHeaderValue,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Bearer <token>") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("演示模式", modifier = Modifier.weight(1f))
                Switch(checked = state.demoMode, onCheckedChange = vm::setDemoMode)
            }
            Text(
                text = "演示模式使用内置示例数据，不请求校方接口。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    keyboard?.hide()
                    vm.save(onBack)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("保存")
                }
            }
        }
    }
}
