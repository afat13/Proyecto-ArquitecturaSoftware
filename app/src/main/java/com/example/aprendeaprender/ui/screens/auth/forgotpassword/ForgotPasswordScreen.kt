package com.example.aprendeaprender.ui.screens.auth.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.components.AppTextField
import com.example.aprendeaprender.ui.components.AuthHeader
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.ui.theme.ErrorRed
import com.example.aprendeaprender.ui.theme.TextGray
import com.example.aprendeaprender.ui.theme.TextWhite

@Composable
fun ForgotPasswordScreen(
    email: String,
    isLoading: Boolean,
    errorResId: Int?,
    successResId: Int?,
    onEmailChange: (String) -> Unit,
    onSendLinkClick: () -> Unit,
    onBackToLoginClick: () -> Unit
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
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        AuthHeader()

        Spacer(modifier = Modifier.height(34.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = stringResource(id = R.string.forgot_password_title),
                color = TextWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.forgot_password_subtitle),
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.forgot_password_description),
                color = TextWhite,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = email,
                label = stringResource(id = R.string.email_label),
                onValueChange = onEmailChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            errorResId?.let { resId ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(id = resId), color = ErrorRed, fontSize = 13.sp)
            }

            successResId?.let { resId ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(id = resId), color = CyanAccent, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppButton(
                text = if (isLoading) stringResource(id = R.string.common_loading)
                else stringResource(id = R.string.send_link_button),
                onClick = onSendLinkClick,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onBackToLoginClick,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(id = R.string.back_to_login), color = CyanAccent)
            }
        }
    }
}