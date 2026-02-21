package com.example.fangbianjizhang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fangbianjizhang.ui.navigation.AppNavHost
import com.example.fangbianjizhang.ui.theme.FangbianTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FangbianTheme {
                AppNavHost()
            }
        }
    }
}
