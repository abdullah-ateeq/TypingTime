package com.example.typingtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.typingtime.ui.navigation.AppNavHost
import com.example.typingtime.ui.theme.TypingTimeTheme
import com.example.typingtime.viewmodel.TypingTimeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TypingTimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val settings = uiState.settings

            TypingTimeTheme(
                themePreference = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
