package com.yourssu.setlog_cloning_androidrookiet1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.RecordScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.SetLogCloningAndroidRookieT1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetLogCloningAndroidRookieT1Theme {
                RecordScreen()
            }
        }
    }
}