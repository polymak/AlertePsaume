package com.bible.alertepsaume

import android.content.Context
import android.content.SharedPreferences

data class FavoriteVerse(
    val psalmIndex: Int,
    val psalmTitle: String,
    val verseNumber: String,
    val verseText: String
)

data class FavoriteChapter(
    val psalmIndex: Int,
    val psalmTitle: String
)

object FavoritesManager {
    private const val PREFS_NAME = "psalm_favorites_prefs"
    private const val KEY_FAVORITES_VERSES = "favorites_set"
    private const val KEY_FAVORITES_CHAPTERS = "favorites_chapters_set"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Verses Favorites ---

    fun isVerseFavorite(context: Context, psalmIndex: Int, verseNumber: String): Boolean {
        val favorites = getPrefs(context).getStringSet(KEY_FAVORITES_VERSES, emptySet()) ?: emptySet()
        return favorites.contains("${psalmIndex}_$verseNumber")
    }

    fun toggleVerseFavorite(context: Context, psalmIndex: Int, verseNumber: String) {
        val prefs = getPrefs(context)
        val favorites = prefs.getStringSet(KEY_FAVORITES_VERSES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val id = "${psalmIndex}_$verseNumber"
        
        if (favorites.contains(id)) {
            favorites.remove(id)
        } else {
            favorites.add(id)
        }
        
        prefs.edit().putStringSet(KEY_FAVORITES_VERSES, favorites).apply()
    }

    fun getFavoriteVerses(context: Context): List<FavoriteVerse> {
        val favorites = getPrefs(context).getStringSet(KEY_FAVORITES_VERSES, emptySet()) ?: emptySet()
        val list = mutableListOf<FavoriteVerse>()
        
        favorites.forEach { id ->
            val parts = id.split("_")
            if (parts.size == 2) {
                val psalmIndex = parts[0].toIntOrNull() ?: return@forEach
                val verseNumber = parts[1]
                
                val psalmText = PsalmData.psalms.getOrNull(psalmIndex) ?: return@forEach
                val psalmTitle = psalmText.substringBefore('\n')
                
                val allVerses = PsalmSelectionManager.splitIntoVerses(psalmText)
                val verse = allVerses.find { 
                    val trimmed = it.trim()
                    trimmed.startsWith("$verseNumber ") || trimmed == verseNumber
                }
                val verseText = verse?.trim()?.substringAfter(" ") ?: ""
                
                list.add(FavoriteVerse(psalmIndex, psalmTitle, verseNumber, verseText))
            }
        }
        return list.sortedWith(compareBy({ it.psalmIndex }, { it.verseNumber.toIntOrNull() ?: 0 }))
    }

    // --- Chapters Favorites ---

    fun isChapterFavorite(context: Context, psalmIndex: Int): Boolean {
        val favorites = getPrefs(context).getStringSet(KEY_FAVORITES_CHAPTERS, emptySet()) ?: emptySet()
        return favorites.contains(psalmIndex.toString())
    }

    fun toggleChapterFavorite(context: Context, psalmIndex: Int) {
        val prefs = getPrefs(context)
        val favorites = prefs.getStringSet(KEY_FAVORITES_CHAPTERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val id = psalmIndex.toString()
        
        if (favorites.contains(id)) {
            favorites.remove(id)
        } else {
            favorites.add(id)
        }
        
        prefs.edit().putStringSet(KEY_FAVORITES_CHAPTERS, favorites).apply()
    }

    fun getFavoriteChapters(context: Context): List<FavoriteChapter> {
        val favorites = getPrefs(context).getStringSet(KEY_FAVORITES_CHAPTERS, emptySet()) ?: emptySet()
        val list = mutableListOf<FavoriteChapter>()
        
        favorites.forEach { id ->
            val psalmIndex = id.toIntOrNull() ?: return@forEach
            val psalmText = PsalmData.psalms.getOrNull(psalmIndex) ?: return@forEach
            val psalmTitle = psalmText.substringBefore('\n')
            
            list.add(FavoriteChapter(psalmIndex, psalmTitle))
        }
        return list.sortedBy { it.psalmIndex }
    }
}
