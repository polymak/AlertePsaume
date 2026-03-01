package com.bible.alertepsaume

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
    var selectedTab by remember { mutableStateOf(0) } // 0 for Verse, 1 for Chapter
    
    var displayedText by remember {
        mutableStateOf(buildAnnotatedString {
            withStyle(style = SpanStyle(color = DarkText, fontSize = 18.sp, fontFamily = FontFamily.Serif)) {
                append("Cliquez sur un bouton ci-dessous pour commencer votre lecture.")
            }
        })
    }
    var cardTitle by remember { mutableStateOf("Psaume...") }
    var showPopup by remember { mutableStateOf(false) }

    val todayDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date())
    }

    fun getVerseOfTheDay() {
        selectedTab = 0
        showPopup = false 
        if (PsalmData.psalms.isEmpty()) return

        val selection = PsalmSelectionManager.getRandomVerseSelection(context)
        val chapterIndex = selection.first
        val startVerseIndex = selection.second
        val psalmText = PsalmData.psalms[chapterIndex]
        cardTitle = psalmText.substringBefore('\n')
        
        val allVerses = PsalmSelectionManager.splitIntoVerses(psalmText)
        val versesToDisplay = mutableListOf<String>()
        val shownIndices = mutableListOf<Int>()
        
        for (i in 0 until 3) {
            val idx = startVerseIndex + i
            if (idx < allVerses.size) {
                versesToDisplay.add(allVerses[idx])
                shownIndices.add(idx)
            }
        }
        PsalmSelectionManager.markVersesShown(context, chapterIndex, shownIndices)

        displayedText = buildAnnotatedString {
            for (verse in versesToDisplay) {
                val verseNumber = verse.trim().substringBefore(" ")
                val verseText = verse.trim().substringAfter(" ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = GoldPrimary)) {
                    append(verseNumber)
                }
                withStyle(style = SpanStyle(color = DarkText, fontSize = 18.sp, fontFamily = FontFamily.Serif)) {
                    append(" $verseText\n\n")
                }
            }
        }
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
        val lines = psalmText.split('\n').filter { it.isNotBlank() }
        cardTitle = lines.first()
        val body = lines.drop(1).joinToString(" ")
        val verses = body.split(Regex(" (?=\\d+ )")).map { it.trim() }.filter { it.isNotBlank() }

        PsalmSelectionManager.markChapterAsRead(context)

        displayedText = buildAnnotatedString {
            for (verse in verses) {
                val verseNumber = verse.substringBefore(" ")
                val verseText = verse.substringAfter(" ", "")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = GoldPrimary)) {
                    append(verseNumber)
                }
                withStyle(style = SpanStyle(color = DarkText, fontSize = 18.sp, fontFamily = FontFamily.Serif)) {
                    append(" $verseText\n\n")
                }
            }
        }
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

            // Title and Underline
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = cardTitle,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(40.dp).height(3.dp).background(GoldPrimary, RoundedCornerShape(2.dp)))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text Card with Scroll Indicator
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                shadowElevation = 2.dp
            ) {
                val scrollState = rememberScrollState()
                val canScrollForward by remember { derivedStateOf { scrollState.canScrollForward } }
                
                AnimatedContent(
                    targetState = displayedText,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    }
                ) { targetText ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = targetText,
                            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp),
                            textAlign = TextAlign.Start,
                            lineHeight = 28.sp,
                            fontFamily = FontFamily.Serif
                        )

                        // Scroll indicator call
                        ScrollIndicator(
                            visible = canScrollForward,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Segmented Buttons
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).background(InactiveGrey.copy(alpha = 0.3f), RoundedCornerShape(30.dp)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Verset du jour button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (selectedTab == 0) GoldPrimary else Color.Transparent)
                        .clickable { getVerseOfTheDay() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Verset du jour", color = if (selectedTab == 0) White else DarkText, fontWeight = FontWeight.Bold)
                }
                // Chapitre du jour button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (selectedTab == 1) GoldPrimary else Color.Transparent)
                        .clickable { getChapterOfTheDay() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chapitre du jour", color = if (selectedTab == 1) White else DarkText, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notification section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GoldPrimary.copy(alpha = 0.05f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable { onActivateNotifications() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldPrimary)
                    Text("Activer la notification quotidienne", color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ScrollIndicator(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, White.copy(alpha = 0.8f))
                        )
                    )
            )
            // Animated arrow
            val infiniteTransition = rememberInfiniteTransition(label = "arrow")
            val dy by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dy"
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = dy.dp).padding(bottom = 8.dp)
            ) {
                Text(
                    "Faites défiler",
                    color = GoldPrimary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
