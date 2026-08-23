package com.example.aprendeaprender.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.TextWhite

@Composable
fun AuthBrandHeader(
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Aprende a Aprender",
            modifier = Modifier.size(88.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aprende a\nAprender",
            color = CyanAccent,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            lineHeight = 34.sp
        )

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu espacio para aprender mejor",
                color = TextWhite.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
        }
    }
}