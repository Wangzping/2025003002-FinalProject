package com.example.campushub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.campushub.navigation.AppNavigation
import com.example.campushub.ui.theme.CampusHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CampusHubApplication
        val preferencesRepo = app.container.userPreferencesRepository

        setContent {
            val darkMode by preferencesRepo.darkMode.collectAsState(initial = false)
            val followSystem by preferencesRepo.followSystem.collectAsState(initial = true)

            CampusHubTheme(darkTheme = darkMode, followSystem = followSystem) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}