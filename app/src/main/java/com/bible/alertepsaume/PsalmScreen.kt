package com.bible.alertepsaume

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.bible.alertepsaume.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsalmScreen(onBack: () -> Unit, onActivateNotifications: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Verse, 1: Chapter, 2: Favorite Verses, 3: Favorite Chapters, 4: Full Chapter View
    
    // Verses to display: List of (PsalmIndex, VerseNumber, VerseText)
    var currentVerses by remember { mutableStateOf<List<Triple<Int, String, String>>>(emptyList()) }
    var currentPsalmIndex by remember { mutableStateOf(-1) }
    var cardTitle by remember { mutableStateOf("Psaume...") }
    var showPopup by remember { mutableStateOf(false) }

    val todayDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date())
    }

    // Tick to force recomposition when favorites change
    var favoritesTick by remember { mutableStateOf(0) }

    fun getVerseOfTheDay() {
        selectedTab = 0
        showPopup = false 
        if (PsalmData.psalms.isEmpty()) return

        val selection = PsalmSelectionManager.getRandomVerseSelection(context)
        val chapterIndex = selection.first
        val startVerseIndex = selection.second
        val psalmText = PsalmData.psalms[chapterIndex]
        currentPsalmIndex = chapterIndex
        cardTitle = psalmText.substringBefore('\n')
        
        val allVerses = PsalmSelectionManager.splitIntoVerses(psalmText)
        val list = mutableListOf<Triple<Int, String, String>>()
        val shownIndices = mutableListOf<Int>()
        
        for (i in 0 until 3) {
            val idx = startVerseIndex + i
            if (idx < allVerses.size) {
                val verse = allVerses[idx].trim()
                val num = verse.substringBefore(" ")
                val txt = verse.substringAfter(" ")
                list.add(Triple(chapterIndex, num, txt))
                shownIndices.add(idx)
            }
        }
        PsalmSelectionManager.markVersesShown(context, chapterIndex, shownIndices)
        currentVerses = list
    }

    fun getChapterOfTheDay() {
        selectedTab = 1
        if (PsalmSelectionManager.isChapterReadToday(context)) {
            showPopup = true
            return
        }
        if (PsalmData.psalms.isEmpty()) return

        val chapterIndex = PsalmSelectionManager.getDailyUiChapterIndex(context)
        val psalmText = PsalmData.psalms[chapterIndex]
        currentPsalmIndex = chapterIndex
        val lines = psalmText.split('\n').filter { it.isNotBlank() }
        cardTitle = lines.first()
        val body = lines.drop(1).joinToString(" ")
        val verses = body.split(Regex(" (?=\\d+ )")).map { it.trim() }.filter { it.isNotBlank() }

        PsalmSelectionManager.markChapterAsRead(context)

        currentVerses = verses.map { 
            val num = it.substringBefore(" ")
            val txt = it.substringAfter(" ", "")
            Triple(chapterIndex, num, txt)
        }
    }

    fun showFullChapter(chapterIndex: Int) {
        selectedTab = 4
        if (PsalmData.psalms.isEmpty()) return

        val psalmText = PsalmData.psalms[chapterIndex]
        currentPsalmIndex = chapterIndex
        val lines = psalmText.split('\n').filter { it.isNotBlank() }
        cardTitle = lines.first()
        val body = lines.drop(1).joinToString(" ")
        val verses = body.split(Regex(" (?=\\d+ )")).map { it.trim() }.filter { it.isNotBlank() }

        currentVerses = verses.map { 
            val num = it.substringBefore(" ")
            val txt = it.substringAfter(" ", "")
            Triple(chapterIndex, num, txt)
        }
    }

    fun showFavoriteVerses() {
        selectedTab = 2
        cardTitle = "Favoris des versets"
    }

    fun showFavoriteChapters() {
        selectedTab = 3
        cardTitle = "Favoris des chapitres"
    }

    if (showPopup) {
        BasicAlertDialog(
            onDismissRequest = { showPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    IconButton(onClick = { showPopup = false }, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = DarkText)
                    }
                    Column(
                        modifier = Modifier.padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Voici la Parole de Dieu qui vous a été réservée pour aujourd’hui. Revenez demain à partir de 07:00.",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            color = DarkText,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).background(White, CircleShape).shadow(2.dp, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DarkText)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("AUJOURD'HUI", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(todayDate, color = DarkText, fontSize = 14.sp)
                }
            }

            // Badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "INSPIRATION DIVINE",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title with Star for Chapters
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cardTitle,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        textAlign = TextAlign.Center
                    )
                    // Only show star if we are reading a chapter (Tab 1 or 4)
                    if ((selectedTab == 1 || selectedTab == 4) && currentPsalmIndex != -1) {
                        val isChapterFav = remember(currentPsalmIndex, favoritesTick) { 
                            FavoritesManager.isChapterFavorite(context, currentPsalmIndex) 
                        }
                        IconButton(onClick = { 
                            FavoritesManager.toggleChapterFavorite(context, currentPsalmIndex)
                            favoritesTick++
                        }) {
                            Icon(
                                imageVector = if (isChapterFav) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = GoldPrimary
                            )
                        }
                    }
                }
                
                // Moved subtitle here for Favorite Chapters view
                if (selectedTab == 3) {
                    Text(
                        text = "Appuyez sur le titre pour lire ce psaume",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(40.dp).height(3.dp).background(GoldPrimary, RoundedCornerShape(2.dp)))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text Card
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                shadowElevation = 2.dp
            ) {
                AnimatedContent(
                    targetState = Triple(selectedTab, currentVerses.hashCode(), favoritesTick),
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "psalmContent"
                ) { (tab, _, _) ->
                    // Move scrollState INSIDE AnimatedContent so it resets on page change
                    val scrollState = rememberScrollState()
                    val canScrollForward by remember { derivedStateOf { scrollState.canScrollForward } }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(24.dp)
                        ) {
                            when (tab) {
                                2 -> {
                                    // Favorite Verses View
                                    val favVerses = FavoritesManager.getFavoriteVerses(context)
                                    if (favVerses.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("Aucun verset favori.", color = DarkText, fontSize = 18.sp, textAlign = TextAlign.Center)
                                        }
                                    } else {
                                        favVerses.forEach { fav ->
                                            FavoriteVerseItem(fav = fav, onToggle = { 
                                                FavoritesManager.toggleVerseFavorite(context, fav.psalmIndex, fav.verseNumber)
                                                favoritesTick++
                                            })
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                                3 -> {
                                    // Favorite Chapters View
                                    val favChapters = FavoritesManager.getFavoriteChapters(context)
                                    if (favChapters.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("Aucun chapitre favori.", color = DarkText, fontSize = 18.sp, textAlign = TextAlign.Center)
                                        }
                                    } else {
                                        favChapters.forEach { fav ->
                                            FavoriteChapterItem(fav = fav, 
                                                onClick = { showFullChapter(fav.psalmIndex) },
                                                onToggle = {
                                                    FavoritesManager.toggleChapterFavorite(context, fav.psalmIndex)
                                                    favoritesTick++
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                                else -> {
                                    // Verse (0), Chapter (1), or Full Chapter (4)
                                    if (currentVerses.isEmpty()) {
                                        Text("Choisissez une lecture.", color = DarkText, fontSize = 18.sp)
                                    } else {
                                        currentVerses.forEach { (pIdx, vNum, vText) ->
                                            val isFav = remember(pIdx, vNum, favoritesTick) { FavoritesManager.isVerseFavorite(context, pIdx, vNum) }
                                            VerseItem(
                                                num = vNum,
                                                text = vText,
                                                isFav = isFav,
                                                onToggleFav = {
                                                    FavoritesManager.toggleVerseFavorite(context, pIdx, vNum)
                                                    favoritesTick++
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                            // Add extra padding at the bottom to ensure last content is visible above the indicator
                            Spacer(modifier = Modifier.height(60.dp))
                        }

                        ScrollIndicator(
                            visible = canScrollForward,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Main Reading Tabs
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(InactiveGrey.copy(alpha = 0.3f), RoundedCornerShape(30.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(28.dp))
                            .background(if (selectedTab == 0) GoldPrimary else Color.Transparent)
                            .clickable { getVerseOfTheDay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Verset du jour", color = if (selectedTab == 0) White else DarkText, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(28.dp))
                            .background(if (selectedTab == 1) GoldPrimary else Color.Transparent)
                            .clickable { getChapterOfTheDay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chapitre du jour", color = if (selectedTab == 1) White else DarkText, fontWeight = FontWeight.Bold)
                    }
                }

                // Favorites Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Favorite Verses
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(30.dp))
                            .background(if (selectedTab == 2) GoldPrimary else InactiveGrey.copy(alpha = 0.3f))
                            .clickable { showFavoriteVerses() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (selectedTab == 2) White else GoldPrimary, modifier = Modifier.size(18.dp))
                            Text("Favoris Versets", color = if (selectedTab == 2) White else DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Favorite Chapters
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(30.dp))
                            .background(if (selectedTab == 3) GoldPrimary else InactiveGrey.copy(alpha = 0.3f))
                            .clickable { showFavoriteChapters() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (selectedTab == 3) White else GoldPrimary, modifier = Modifier.size(18.dp))
                            Text("Favoris Chapitres", color = if (selectedTab == 3) White else DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Notification section
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .background(GoldPrimary.copy(alpha = 0.05f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .clickable { onActivateNotifications() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Text("Activer la notification quotidienne", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun VerseItem(num: String, text: String, isFav: Boolean, onToggleFav: () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = num, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 18.sp, modifier = Modifier.width(30.dp))
        Text(text = text, color = DarkText, fontSize = 18.sp, fontFamily = FontFamily.Serif, lineHeight = 28.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleFav, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = GoldPrimary)
        }
    }
}

@Composable
fun FavoriteVerseItem(fav: FavoriteVerse, onToggle: () -> Unit) {
    Column {
        Text(text = fav.psalmTitle, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Top) {
            Text(text = fav.verseNumber, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 18.sp, modifier = Modifier.width(30.dp))
            Text(text = fav.verseText, color = DarkText, fontSize = 18.sp, fontFamily = FontFamily.Serif, lineHeight = 28.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary)
            }
        }
        HorizontalDivider(color = InactiveGrey.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun FavoriteChapterItem(fav: FavoriteChapter, onClick: () -> Unit, onToggle: () -> Unit) {
    Surface(
        onClick = onClick,
        color = White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fav.psalmTitle,
                color = DarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldPrimary)
            }
        }
    }
}

@Composable
private fun ScrollIndicator(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).background(Brush.verticalGradient(colors = listOf(Color.Transparent, White.copy(alpha = 0.8f)))))
            val infiniteTransition = rememberInfiniteTransition(label = "arrow")
            val dy by infiniteTransition.animateFloat(0f, 6f, infiniteRepeatable(tween(1000, easing = LinearOutSlowInEasing), RepeatMode.Reverse), label = "dy")
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = dy.dp).padding(bottom = 8.dp)) {
                Text("Faites défiler", color = GoldPrimary.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GoldPrimary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }
}
