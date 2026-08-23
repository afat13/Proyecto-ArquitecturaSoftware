package com.example.aprendeaprender.ui.screens.ai

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.ai.GemmaModelManager
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.ui.theme.ErrorRed
import com.example.aprendeaprender.ui.theme.TextGray
import com.example.aprendeaprender.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun AiModelDownloadScreen(
    modelManager: GemmaModelManager,
    onModelReady: () -> Unit
) {
    val uiState by modelManager.uiState.collectAsState()

    LaunchedEffect(modelManager) {
        while (true) {
            val ready = modelManager.prepararModelo(descargarSiNoExiste = true)
            if (ready) {
                onModelReady()
                break
            }

            if (modelManager.uiState.value.error != null) {
                break
            }

            delay(700)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = stringResource(R.string.logo_app_desc),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(92.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.ai_model_download_title),
            color = TextWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = uiState.mensaje,
            color = TextGray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        when {
            uiState.error != null -> {
                Text(
                    text = uiState.error.orEmpty(),
                    color = ErrorRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { modelManager.reintentarDescarga() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.ai_model_download_retry),
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            uiState.descargando -> {
                LinearProgressIndicator(
                    progress = { uiState.progreso / 100f },
                    color = CyanAccent,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.ai_model_download_progress, uiState.progreso),
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            else -> {
                CircularProgressIndicator(color = CyanAccent)
            }
        }
    }
}
