package com.bible.alertepsaume

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Mise à jour de la sélection intelligente pour la notification
        PsalmSelectionManager.updateNotifContentIfNeeded(context)
        
        // 2. Récupération des données du chapitre réservé pour la notification (Cycle 11:00)
        val chapterData = getDailyChapter(context)

        // 3. Intention pour ouvrir l'app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            openAppIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construction de la notification avec expiration à 23:58
        // Note: setTimeoutAfter gère l'auto-suppression par le système
        val builder = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_app)
            .setContentTitle(chapterData.title)
            .setContentText(chapterData.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(chapterData.content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(calculateTimeoutMillis()) // Expire à 23:58
            .setGroup("daily_psalm") // Assure qu'une seule notification est affichée

        // 5. Affichage
        with(NotificationManagerCompat.from(context)) {
            try {
                 notify(NotificationUtils.NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Permission POST_NOTIFICATIONS non accordée
            }
        }
    }

    private fun getDailyChapter(context: Context): ChapterData {
        if (PsalmData.psalms.isEmpty()) {
            return ChapterData("AlertePsaume", "Aucun psaume n'a été chargé.")
        }

        val chapterIndex = PsalmSelectionManager.getDailyNotifChapterIndex(context)
        val psalmText = PsalmData.psalms[chapterIndex]
        
        val chapterTitle = psalmText.substringBefore('\n')
        // On affiche le corps du chapitre proprement
        val chapterBody = psalmText.substringAfter('\n').replace(Regex(" (\\d+ )"), "\n$1")

        return ChapterData(chapterTitle, chapterBody)
    }

    /**
     * Calcule le temps restant jusqu'à 23:58 aujourd'hui en millisecondes.
     */
    private fun calculateTimeoutMillis(): Long {
        val now = java.util.Calendar.getInstance()
        val timeout = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 58)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val diff = timeout.timeInMillis - now.timeInMillis
        return if (diff > 0) diff else 0L
    }

    data class ChapterData(val title: String, val content: String)
}
