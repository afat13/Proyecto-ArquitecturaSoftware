package com.example.aprendeaprender.ui.screens.auth.verifyemail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.components.AuthHeader
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.ui.theme.ErrorRed
import com.example.aprendeaprender.ui.theme.TextGray
import com.example.aprendeaprender.ui.theme.TextWhite
import com.example.aprendeaprender.viewmodel.VerifyEmailUiState

@Composable
fun VerifyEmailScreen(
    uiState: VerifyEmailUiState,
    onReenviarCorreoClick: () -> Unit,
    onYaVerifiqueClick: () -> Unit,
    onVolverLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader()

        Spacer(modifier = Modifier.height(34.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.verify_email_title),
                color = TextWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.verify_email_subtitle),
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.verify_email_body, uiState.correo),
                color = TextWhite,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            uiState.mensajeErrorResId?.let { errorRes ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(id = errorRes), color = ErrorRed, fontSize = 14.sp)
            }

            uiState.mensajeExitoResId?.let { successRes ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(id = successRes), color = CyanAccent, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppButton(
                text = if (uiState.cargando) stringResource(id = R.string.common_loading)
                else stringResource(id = R.string.verify_email_check_action),
                onClick = onYaVerifiqueClick,
                enabled = !uiState.cargando
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppButton(
                text = stringResource(id = R.string.verify_email_resend_action),
                onClick = onReenviarCorreoClick,
                enabled = !uiState.cargando
            )

            if (uiState.cargando) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = CyanAccent,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onVolverLoginClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(id = R.string.verify_email_back_to_login),
                    color = CyanAccent
                )
            }
        }
    }
}