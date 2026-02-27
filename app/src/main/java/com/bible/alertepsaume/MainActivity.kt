package com.bible.alertepsaume

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bible.alertepsaume.ui.theme.AlertePsaumeTheme
import com.bible.alertepsaume.ui.theme.HomeScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationUtils.scheduleDailyNotification(this)
            Toast.makeText(this, "Notifications quotidiennes activées !", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission refusée. Les notifications ne seront pas envoyées.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Correction : installSplashScreen() doit être appelé AVANT super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        NotificationUtils.createNotificationChannel(this)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)

        setContent {
            AlertePsaumeTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("psalm") { PsalmScreen(::askForNotificationPermission) }
                }

                // Déclenchement de la demande de permission après le premier affichage
                if (isFirstLaunch) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        askForNotificationPermission()
                        prefs.edit().putBoolean("is_first_launch", false).apply()
                    }
                }
            }
        }

        if (!isFirstLaunch && prefs.getBoolean("notifications_enabled", false)) {
            NotificationUtils.scheduleDailyNotification(this)
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationUtils.scheduleDailyNotification(this)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationUtils.scheduleDailyNotification(this)
        }
    }
}
