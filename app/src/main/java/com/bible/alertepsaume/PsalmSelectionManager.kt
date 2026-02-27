package com.bible.alertepsaume

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

object PsalmSelectionManager {
    private const val PREFS_NAME = "psalm_selection_prefs"
    private const val KEY_SHOWN_CHAPTERS = "shown_chapters"
    private const val KEY_COMPLETED_VERSES_CHAPTERS = "completed_verses_chapters"
    
    private const val KEY_LAST_REFRESH_MILLIS_UI = "last_refresh_millis_ui"
    private const val KEY_LAST_REFRESH_MILLIS_NOTIF = "last_refresh_millis_notif"
    
    private const val KEY_UI_CHAPTER_INDEX = "ui_chapter_index"
    private const val KEY_UI_VERSE_CHAPTER_INDEX = "ui_verse_chapter_index"
    private const val KEY_UI_VERSE_START_INDEX = "ui_verse_start_index"
    
    private const val KEY_NOTIF_CHAPTER_INDEX = "notif_chapter_index"
    
    private const val KEY_HAS_READ_VERSE = "has_read_verse_today"
    private const val KEY_HAS_READ_CHAPTER = "has_read_chapter_today"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Cycle UI : 07:00 à 06:59.
     */
    fun updateUiContentIfNeeded(context: Context) {
        val prefs = getPrefs(context)
        val lastRefresh = prefs.getLong(KEY_LAST_REFRESH_MILLIS_UI, 0L)
        val now = System.currentTimeMillis()
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        if (calendar.get(Calendar.HOUR_OF_DAY) < 7) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 7)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (lastRefresh < calendar.timeInMillis) {
            val nextChapter = generateNextChapterIndex(context)
            val (vChapter, vStart) = generateNextVerseSelection(context)
            
            prefs.edit()
                .putLong(KEY_LAST_REFRESH_MILLIS_UI, now)
                .putInt(KEY_UI_CHAPTER_INDEX, nextChapter)
                .putInt(KEY_UI_VERSE_CHAPTER_INDEX, vChapter)
                .putInt(KEY_UI_VERSE_START_INDEX, vStart)
                .putBoolean(KEY_HAS_READ_VERSE, false)
                .putBoolean(KEY_HAS_READ_CHAPTER, false)
                .apply()
        }
    }

    /**
     * Cycle Notification : 11:00 à 10:59.
     */
    fun updateNotifContentIfNeeded(context: Context) {
        val prefs = getPrefs(context)
        val lastRefresh = prefs.getLong(KEY_LAST_REFRESH_MILLIS_NOTIF, 0L)
        val now = System.currentTimeMillis()
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        if (calendar.get(Calendar.HOUR_OF_DAY) < 11) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 11)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (lastRefresh < calendar.timeInMillis) {
            val nextChapter = generateNextChapterIndex(context)
            prefs.edit()
                .putLong(KEY_LAST_REFRESH_MILLIS_NOTIF, now)
                .putInt(KEY_NOTIF_CHAPTER_INDEX, nextChapter)
                .apply()
        }
    }

    fun getDailyUiChapterIndex(context: Context): Int {
        updateUiContentIfNeeded(context)
        return getPrefs(context).getInt(KEY_UI_CHAPTER_INDEX, 0)
    }

    fun getDailyUiVerseSelection(context: Context): Pair<Int, Int> {
        updateUiContentIfNeeded(context)
        val prefs = getPrefs(context)
        return Pair(
            prefs.getInt(KEY_UI_VERSE_CHAPTER_INDEX, 0),
            prefs.getInt(KEY_UI_VERSE_START_INDEX, 0)
        )
    }
    
    /**
     * Retourne une sélection aléatoire de versets (non liée au cycle quotidien).
     */
    fun getRandomVerseSelection(context: Context): Pair<Int, Int> {
        return generateNextVerseSelection(context)
    }

    fun getDailyNotifChapterIndex(context: Context): Int {
        updateNotifContentIfNeeded(context)
        return getPrefs(context).getInt(KEY_NOTIF_CHAPTER_INDEX, 0)
    }

    fun markVerseAsRead(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_HAS_READ_VERSE, true).apply()
    }

    fun markChapterAsRead(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_HAS_READ_CHAPTER, true).apply()
    }

    fun isChapterReadToday(context: Context): Boolean {
        updateUiContentIfNeeded(context)
        return getPrefs(context).getBoolean(KEY_HAS_READ_CHAPTER, false)
    }

    fun isVerseReadToday(context: Context): Boolean {
        updateUiContentIfNeeded(context)
        return getPrefs(context).getBoolean(KEY_HAS_READ_VERSE, false)
    }

    private fun generateNextChapterIndex(context: Context): Int {
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

    fun generateNextVerseSelection(context: Context): Pair<Int, Int> {
        val prefs = getPrefs(context)
        val completedChapters = prefs.getStringSet(KEY_COMPLETED_VERSES_CHAPTERS, emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        val totalChapters = PsalmData.psalms.size
        var availableChapters = (0 until totalChapters).filter { it !in completedChapters }
        
        if (availableChapters.isEmpty()) {
            val editor = prefs.edit()
            for (i in 0 until totalChapters) {
                editor.remove("shown_verses_ch_$i")
            }
            editor.putStringSet(KEY_COMPLETED_VERSES_CHAPTERS, emptySet())
            editor.apply()
            availableChapters = (0 until totalChapters).toList()
        }
        
        val chapterIndex = availableChapters.random()
        val shownVerses = prefs.getStringSet("shown_verses_ch_$chapterIndex", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        val allVerses = splitIntoVerses(PsalmData.psalms[chapterIndex])
        
        val availableVerses = (0 until allVerses.size).filter { it !in shownVerses }
        
        if (availableVerses.isEmpty()) {
            markChapterVersesCompleted(context, chapterIndex)
            return generateNextVerseSelection(context)
        }
        
        val verseIndex = availableVerses.random()
        return Pair(chapterIndex, verseIndex)
    }

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
        val bodyWithNewlines = chapterBody.replace(Regex(" (\\d+ )"), "\n$1")
        return bodyWithNewlines.split('\n').filter { it.isNotBlank() }
    }
}
