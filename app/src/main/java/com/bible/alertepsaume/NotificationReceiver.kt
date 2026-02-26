package com.bible.alertepsaume

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Logique pour obtenir le chapitre du jour
        val chapterData = getDailyChapter(context)

        // 2. Créer une intention pour ouvrir l'application lorsque l'on clique sur la notification
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        // 3. Construire la notification
        val builder = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_app) // Assurez-vous que R.drawable.logo_app existe
            .setContentTitle(chapterData.title)
            .setContentText(chapterData.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(chapterData.content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // La notification disparaît après le clic

        // 4. Afficher la notification
        with(NotificationManagerCompat.from(context)) {
            try {
                 notify(1, builder.build())
            } catch (e: SecurityException) {
                // Gérer le cas où la permission est révoquée
            }
        }
    }

    private fun getDailyChapter(context: Context): ChapterData {
        if (PsalmData.psalms.isEmpty()) {
            return ChapterData("AlertePsaume", "Aucun psaume n'a été chargé.")
        }

        val randomPsalmChapter = PsalmData.psalms.random()
        val chapterTitle = randomPsalmChapter.substringBefore('\n')
        val chapterBody = randomPsalmChapter.substringAfter('\n').replace(Regex(" (\\d+ )"), "\n$1")

        return ChapterData(chapterTitle, chapterBody)
    }

    data class ChapterData(val title: String, val content: String)
}