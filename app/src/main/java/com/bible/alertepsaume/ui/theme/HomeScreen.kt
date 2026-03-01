package com.bible.alertepsaume.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 30.dp, vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper part
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            // Icon with circle background
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(GoldPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = GoldPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Title
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = DarkText,
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("Bienvenue sur\n")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Alerte Psaume")
                    }
                },
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Éphésiens 5:19",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif
            )
        }

        // Middle Citation
        Text(
            text = "\"LES PSAUMES SONT DES PAROLES PUISSANTES TOUT AU LONG DE NOTRE VIE\"",
            color = DarkText,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        // Bottom Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(4.dp, RoundedCornerShape(30.dp))
                .background(GoldPrimary, RoundedCornerShape(30.dp))
                .clickable { navController.navigate("psalm") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "COMMENCER LA LECTURE",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
