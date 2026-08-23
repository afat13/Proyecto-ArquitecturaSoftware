package com.example.aprendeaprender.ui.screens.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import com.example.aprendeaprender.ui.components.TermsDialog
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.ui.theme.ErrorRed
import com.example.aprendeaprender.ui.theme.TextGray
import com.example.aprendeaprender.ui.theme.TextWhite

@Composable
fun RegisterScreen(
    nombre: String,
    apellido: String,
    edad: String,
    carrera: String,
    email: String,
    password: String,
    confirmPassword: String,
    acceptedTerms: Boolean,
    isLoading: Boolean,
    errorResId: Int?,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onEdadChange: (String) -> Unit,
    onCarreraChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var showTermsDialog by remember { mutableStateOf(false) }

    if (showTermsDialog) {
        TermsDialog(
            onAccept = {
                onAcceptedTermsChange(true)
                showTermsDialog = false
            },
            onDismiss = { showTermsDialog = false }
        )
    }

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
        Spacer(modifier = Modifier.height(48.dp))

        AuthHeader()

        Spacer(modifier = Modifier.height(34.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = stringResource(id = R.string.register_title),
                color = TextWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.register_subtitle),
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = nombre,
                label = stringResource(id = R.string.first_name_label),
                onValueChange = onNombreChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = apellido,
                label = stringResource(id = R.string.last_name_label),
                onValueChange = onApellidoChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = edad,
                label = stringResource(id = R.string.age_label),
                onValueChange = onEdadChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = carrera,
                label = stringResource(id = R.string.career_label),
                onValueChange = onCarreraChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = email,
                label = stringResource(id = R.string.email_label),
                onValueChange = onEmailChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = password,
                label = stringResource(id = R.string.password_label),
                onValueChange = onPasswordChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                value = confirmPassword,
                label = stringResource(id = R.string.confirm_password_label),
                onValueChange = onConfirmPasswordChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = onAcceptedTermsChange,
                    enabled = !isLoading
                )
                TextButton(onClick = { showTermsDialog = true }, enabled = !isLoading) {
                    Text(text = stringResource(id = R.string.accept_terms_label), color = TextWhite)
                }
            }

            TextButton(onClick = { showTermsDialog = true }, enabled = !isLoading) {
                Text(text = stringResource(id = R.string.view_terms_label), color = CyanAccent)
            }

            errorResId?.let { resId ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(id = resId), color = ErrorRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            AppButton(
                text = if (isLoading) stringResource(id = R.string.common_loading)
                else stringResource(id = R.string.register_button),
                onClick = onRegisterClick,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(id = R.string.register_have_account),
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

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