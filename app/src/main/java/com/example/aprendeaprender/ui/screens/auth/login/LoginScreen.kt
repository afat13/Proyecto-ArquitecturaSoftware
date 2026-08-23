package com.example.aprendeaprender.ui.screens.auth.login

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun LoginScreen(
    email: String,
    password: String,
    isLoading: Boolean,
    errorResId: Int?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit
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
                text = stringResource(id = R.string.login_title),
                color = TextWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.login_subtitle),
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = email,
                label = stringResource(id = R.string.email_label),
                onValueChange = onEmailChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = password,
                label = stringResource(id = R.string.password_label),
                onValueChange = onPasswordChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )

            errorResId?.let { resId ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = resId),
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppButton(
                text = if (isLoading) stringResource(id = R.string.common_loading)
                else stringResource(id = R.string.login_button),
                onClick = onLoginClick,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onForgotPasswordClick,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(id = R.string.forgot_password_link), color = CyanAccent)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.login_no_account),
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            TextButton(
                onClick = onRegisterClick,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(id = R.string.register_link), color = CyanAccent)
            }
        }
    }
}