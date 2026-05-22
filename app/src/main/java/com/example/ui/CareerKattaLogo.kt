package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CareerKattaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE0F2FE), Color.White, Color(0xFFE0F2FE))
                )
            )
            .border(2.dp, Color(0xFF93C5FD), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            // Stylized Icon: Book representing Career Katta brand identity with a glowing circular design
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(size * 0.35f)
            ) {
                // Background deep blue circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF1E40AF))
                )
                
                // Book icon
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Career Katta Icon",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.20f)
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // "करिअर कट्टा" (Large bold custom Devanagari logo text)
            Text(
                text = "करिअर कट्टा",
                style = TextStyle(
                    fontSize = (size.value * 0.11f).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD946EF), // Pinkish crimson brand color
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color(0xFFFBBF24), // Yellow border/shadow glow effect
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                        blurRadius = 1f
                    )
                )
            )
            
            // Subtitle: "युवकांच्या सर्वांगीण विकासासाठी"
            Text(
                text = "युवकांच्या\nसर्वांगीण विकासासाठी",
                style = TextStyle(
                    fontSize = (size.value * 0.052f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A), // Dark blue brand color
                    lineHeight = (size.value * 0.065f).sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
