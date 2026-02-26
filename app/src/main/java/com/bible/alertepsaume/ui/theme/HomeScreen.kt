package com.bible.alertepsaume.ui.theme

import com.bible.alertepsaume.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.navigation.NavController
import kotlin.random.Random

// Couleurs de la capture : nuages dorés, ambre, lumière
private val GoldLightTop = Color(0xFFFFF9E6)
private val GoldCloudMid = Color(0xFFF5C96B)
private val GoldCloudDark = Color(0xFFC9952E)
private val AmberDark = Color(0xFFA67C2E)
private val TextGoldenBeige = Color(0xFFF5E6C8)
private val TextGoldenGlow = Color(0xFFE8D4A8)
private val ButtonGoldCenter = Color(0xFFFFE082)
private val ButtonGoldEdge = Color(0xFFD4A017)
private val ButtonTextDark = Color(0xFF2C1810)

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GoldLightTop,
                        Color(0xFFFFF0D0),
                        GoldCloudMid,
                        Color(0xFFE8B84A),
                        GoldCloudDark,
                        AmberDark
                    )
                )
            )
    ) {
        // Paillettes / étoiles scintillantes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            repeat(80) {
                val x = Random.nextFloat() * w
                val y = Random.nextFloat() * h
                val r = Random.nextFloat() * 2f + 0.5f
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawCircle(x, y, r, android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(180, 255, 248, 220)
                        isAntiAlias = true
                    })
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Lyre dorée centrale (hero)
            Image(
                painter = painterResource(id = R.drawable.ic_lyre_golden),
                contentDescription = null,
                modifier = Modifier
                    .height(180.dp)
                    .width(140.dp),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )

            // Bloc texte : titre + description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Titre : "Bienvenue sur" (plus petit) + "Alerte Psaume" (plus grand, gras) — sans cadre
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = TextGoldenBeige,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal
                            )
                        ) {
                            append("Bienvenue sur\n")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = TextGoldenBeige,
                                fontSize = 36.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Alerte Psaume")
                        }
                    },
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Verset Éphésiens 5:19 
                Text(
                    text = "Éphésiens 5:19",
                    fontSize = 14.sp,
                    color = TextGoldenGlow,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "LES PSAUMES SONT DES PAROLES PUISSANTES TOUT AU LONG DE NOTRE VIE",
                    fontSize = 17.sp,
                    color = TextGoldenGlow,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Bouton CTA : dégradé doré (centre clair, bords plus foncés), texte noir, relief
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFD4A017).copy(alpha = 0.5f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                ButtonGoldEdge,
                                ButtonGoldCenter,
                                Color(0xFFFFE082),
                                ButtonGoldCenter,
                                ButtonGoldEdge
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = { navController.navigate("psalm") }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "COMMENCER LA LECTURE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ButtonTextDark,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
