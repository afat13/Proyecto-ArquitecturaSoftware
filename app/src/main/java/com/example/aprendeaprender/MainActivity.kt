package com.example.aprendeaprender

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aprendeaprender.data.notifications.ChatPollWorker
import com.example.aprendeaprender.data.notifications.TaskPollWorker
import com.example.aprendeaprender.navigation.AppNavHost
import com.example.aprendeaprender.ui.theme.AprendeAprenderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ChatPollWorker.registrar(this)
        TaskPollWorker.registrar(this)
        pedirPermisoNotificaciones()

        setContent {
            AprendeAprenderTheme {
                AppNavHost()
            }
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permiso), 1001)
            }
        }
    }
}
