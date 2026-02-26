package com.bible.alertepsaume

import com.bible.alertepsaume.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun PsalmScreen(onActivateNotifications: () -> Unit) {
    var displayedText by remember {
        mutableStateOf(buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Black, fontSize = 18.sp, fontFamily = FontFamily.Serif)) {
                append("Choisissez une lecture pour commencer.")
            }
        })
    }
    var cardTitle by remember { mutableStateOf("Psaume...") }

    fun getVerseOfTheDay() {
        if (PsalmData.psalms.isEmpty()) {
            displayedText = buildAnnotatedString { append("Aucun psaume n\'a été chargé.") }
            return
        }

        val randomPsalmChapter = PsalmData.psalms.random()
        val chapterTitle = randomPsalmChapter.substringBefore('\n')
        cardTitle = chapterTitle
        val chapterBody = randomPsalmChapter.substringAfter('\n')
        val bodyWithNewlines = chapterBody.replace(Regex(" (\\d+ )"), "\n$1")
        val allVerses = bodyWithNewlines.split('\n').filter { it.isNotBlank() }

        if (allVerses.isEmpty()) { return }

        var currentIndex = Random.nextInt(0, allVerses.size)
        val firstVerse = allVerses[currentIndex]
        val versesToDisplay = mutableListOf<String>()
        versesToDisplay.add(firstVerse)

        if (firstVerse.trim().startsWith("Au chef des chantres")) {
            if (currentIndex + 1 < allVerses.size) {
                versesToDisplay.add(allVerses[currentIndex + 1])
                currentIndex++
            }
        }

        while (versesToDisplay.last().trim().let { it.endsWith("...") || it.endsWith(",") || it.endsWith("?") || it.endsWith(":") } && currentIndex + 1 < allVerses.size) {
            versesToDisplay.add(allVerses[currentIndex + 1])
            currentIndex++
        }

        displayedText = buildAnnotatedString {
            for (verse in versesToDisplay) {
                val verseNumber = verse.trim().substringBefore(" ")
                val verseText = verse.trim().substringAfter(" ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFE53935))) {
                    append(verseNumber)
                }
                withStyle(style = SpanStyle(color = Color.DarkGray, fontSize = 18.sp)) {
                    append(" $verseText\n")
                }
            }
        }
    }

    fun getChapterOfTheDay() {
        if (PsalmData.psalms.isEmpty()) {
            displayedText = buildAnnotatedString { append("Aucun psaume n\'a été chargé.") }
            return
        }

        val randomPsalmChapter = PsalmData.psalms.random()
        val lines = randomPsalmChapter.split('\n').filter { it.isNotBlank() }
        val chapterTitle = lines.first()
        cardTitle = chapterTitle
        val body = lines.drop(1).joinToString(" ")
        val verses = body.split(Regex(" (?=\\d+ )")).map { it.trim() }.filter { it.isNotBlank() }

        displayedText = buildAnnotatedString {
            for (verse in verses) {
                val verseNumber = verse.substringBefore(" ")
                val verseText = verse.substringAfter(" ", "")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFE53935))) {
                    append(verseNumber)
                }
                withStyle(style = SpanStyle(color = Color.DarkGray, fontSize = 18.sp)) {
                    append(" $verseText\n")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF9E6),
                        Color(0xFFFFF0D0),
                        Color(0xFFF5C96B),
                        Color(0xFFE8B84A),
                        Color(0xFFC9952E),
                        Color(0xFFA67C2E)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(60) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                val r = Random.nextFloat() * 1.5f + 0.5f
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = r,
                    center = Offset(x, y)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.92f),
                tonalElevation = 2.dp,
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cardTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238),
                            fontFamily = FontFamily.SansSerif
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_lyre_golden),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AnimatedContent(
                        targetState = displayedText,
                        label = "psalmTextAnimation",
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 20.dp)
                    ) { targetText ->
                        val scrollState = rememberScrollState()
                        val showScrollIndicator by remember {
                            derivedStateOf { scrollState.canScrollForward }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = targetText,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                textAlign = TextAlign.Start,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily.Serif
                            )

                            val infiniteTransition = rememberInfiniteTransition()
                            val position by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            if (showScrollIndicator) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Faire défiler",
                                    tint = Color.Black.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp)
                                        .size(32.dp)
                                        .offset(y = position.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VerseButton(
                        onClick = { getVerseOfTheDay() },
                        modifier = Modifier.weight(1f)
                    )
                    
                    ChapterButton(
                        onClick = { getChapterOfTheDay() },
                        modifier = Modifier.weight(1f)
                    )
                }

                NotificationButton(
                    onClick = { onActivateNotifications() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun VerseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 3.dp else 6.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFFE53935)
            )
            Text(
                text = "Verset du jour",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF263238)
            )
        }
    }
}

@Composable
fun ChapterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 3.dp else 6.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0xFFD4A017).copy(alpha = 0.25f)
            )
            .background(
                color = Color(0xFFFFD54F),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF1A1A1A)
            )
            Text(
                text = "Chapitre du jour",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
fun NotificationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(29.dp),
                spotColor = Color(0xFF1565C0).copy(alpha = 0.35f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1565C0),
                        Color(0xFF1976D2),
                        Color(0xFF42A5F5)
                    )
                ),
                shape = RoundedCornerShape(29.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            repeat(25) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                val r = Random.nextFloat() * 1.2f + 0.4f
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = r,
                    center = Offset(x, y)
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFFFFD54F)
            )
            Text(
                text = "Activer les notifications quotidiennes",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
        }
    }
}
