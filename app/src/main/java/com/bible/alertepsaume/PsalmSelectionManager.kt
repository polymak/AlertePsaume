package com.bible.alertepsaume

import android.content.Context
import android.content.SharedPreferences

object PsalmSelectionManager {
    private const val PREFS_NAME = "psalm_selection_prefs"
    private const val KEY_SHOWN_CHAPTERS = "shown_chapters"
    private const val KEY_COMPLETED_VERSES_CHAPTERS = "completed_verses_chapters"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retourne l'index d'un chapitre non encore affiché dans le cycle actuel pour "Chapitre du jour".
     */
    fun getNextChapterIndex(context: Context): Int {
        val prefs = getPrefs(context)
        val shown = prefs.getStringSet(KEY_SHOWN_CHAPTERS, emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        
        val total = PsalmData.psalms.size
        var available = (0 until total).filter { it !in shown }
        
        if (available.isEmpty()) {
            prefs.edit().putStringSet(KEY_SHOWN_CHAPTERS, emptySet()).apply()
            available = (0 until total).toList()
        }
        
        val chosen = available.random()
        val newShown = shown.toMutableSet()
        newShown.add(chosen)
        prefs.edit().putStringSet(KEY_SHOWN_CHAPTERS, newShown.map { it.toString() }.toSet()).apply()
        
        return chosen
    }

    /**
     * Retourne un couple (index du chapitre, index du verset de départ) non encore affiché.
     */
    fun getNextVerseSelection(context: Context): Pair<Int, Int> {
        val prefs = getPrefs(context)
        
        // 1. Choisir un chapitre qui a encore des versets non affichés
        val completedChapters = prefs.getStringSet(KEY_COMPLETED_VERSES_CHAPTERS, emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        val totalChapters = PsalmData.psalms.size
        var availableChapters = (0 until totalChapters).filter { it !in completedChapters }
        
        if (availableChapters.isEmpty()) {
            // Réinitialiser tout pour les versets
            val editor = prefs.edit()
            for (i in 0 until totalChapters) {
                editor.remove("shown_verses_ch_$i")
            }
            editor.putStringSet(KEY_COMPLETED_VERSES_CHAPTERS, emptySet())
            editor.apply()
            availableChapters = (0 until totalChapters).toList()
        }
        
        val chapterIndex = availableChapters.random()
        
        // 2. Choisir un verset non affiché dans ce chapitre
        val shownVerses = prefs.getStringSet("shown_verses_ch_$chapterIndex", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        
        val psalmText = PsalmData.psalms[chapterIndex]
        val allVerses = splitIntoVerses(psalmText)
        
        val availableVerses = (0 until allVerses.size).filter { it !in shownVerses }
        
        if (availableVerses.isEmpty()) {
            // Sécurité : marquer comme complété et réessayer
            markChapterVersesCompleted(context, chapterIndex)
            return getNextVerseSelection(context)
        }
        
        val verseIndex = availableVerses.random()
        return Pair(chapterIndex, verseIndex)
    }

    /**
     * Marque un ou plusieurs versets comme affichés.
     */
    fun markVersesShown(context: Context, chapterIndex: Int, verseIndices: List<Int>) {
        val prefs = getPrefs(context)
        val key = "shown_verses_ch_$chapterIndex"
        val shown = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        
        shown.addAll(verseIndices.map { it.toString() })
        
        val psalmText = PsalmData.psalms[chapterIndex]
        val totalVerses = splitIntoVerses(psalmText).size
        
        prefs.edit().putStringSet(key, shown).apply()
        
        if (shown.size >= totalVerses) {
            markChapterVersesCompleted(context, chapterIndex)
        }
    }

    private fun markChapterVersesCompleted(context: Context, chapterIndex: Int) {
        val prefs = getPrefs(context)
        val completed = prefs.getStringSet(KEY_COMPLETED_VERSES_CHAPTERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        completed.add(chapterIndex.toString())
        prefs.edit().putStringSet(KEY_COMPLETED_VERSES_CHAPTERS, completed).apply()
    }

    fun splitIntoVerses(psalmText: String): List<String> {
        val chapterBody = psalmText.substringAfter('\n')
        // Regex pour identifier les versets commençant par un numéro
        val bodyWithNewlines = chapterBody.replace(Regex(" (\\d+ )"), "\n$1")
        return bodyWithNewlines.split('\n').filter { it.isNotBlank() }
    }
}
