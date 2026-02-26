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
        super.onCreate(savedInstanceState)
        installSplashScreen()
        NotificationUtils.createNotificationChannel(this)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            askForNotificationPermission() // Demande automatique au premier lancement
            prefs.edit().putBoolean("is_first_launch", false).apply()
        } else if (prefs.getBoolean("notifications_enabled", false)) {
            // Assure que la notif est reprogrammée si l'app est juste redémarrée (pas le tel)
            NotificationUtils.scheduleDailyNotification(this)
        }

        setContent {
            AlertePsaumeTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("psalm") { PsalmScreen(::askForNotificationPermission) } // On laisse le bouton pour réactiver
                }
            }
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // La permission est déjà accordée
                    NotificationUtils.scheduleDailyNotification(this)
                    // Optionnel: afficher un toast seulement si l'utilisateur clique sur le bouton, pas au lancement
                    // Toast.makeText(this, "Notifications quotidiennes déjà activées !", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Expliquer pourquoi la permission est utile avant de la redemander
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Demander la permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Pas besoin de demander la permission pour les versions antérieures
            NotificationUtils.scheduleDailyNotification(this)
            // Optionnel: afficher un toast seulement si l'utilisateur clique sur le bouton
            // Toast.makeText(this, "Notifications quotidiennes activées !", Toast.LENGTH_SHORT).show()
        }
    }
}