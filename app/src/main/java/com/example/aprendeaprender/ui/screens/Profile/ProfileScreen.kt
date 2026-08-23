package com.example.aprendeaprender.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onCerrarSesionClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensajeExitoResId) {
        if (uiState.mensajeExitoResId != null) {
            isEditing = false
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    stringResource(R.string.profile_logout_title),
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Text(
                    stringResource(R.string.profile_logout_message),
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onCerrarSesionClick()
                }) {
                    Text(stringResource(R.string.profile_btn_logout), color = CyanAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.profile_btn_cancel), color = TextGray)
                }
            }
        )
    }

    Scaffold(containerColor = DarkBackground) { innerPadding ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyanAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                IconButton(onClick = {
                    if (isEditing) isEditing = false else onBackClick()
                }) {
                    Text("‹", fontSize = 32.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                }

                val nombreCompleto = "${uiState.nombre} ${uiState.apellido}".trim()
                Text(
                    text = nombreCompleto.ifBlank { uiState.correo },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyanAccent, CircleShape)
                        .background(DarkSurface)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = stringResource(R.string.profile_cd_photo),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isEditing) {
                    ProfileEditableField(
                        label = stringResource(R.string.first_name_label),
                        value = uiState.nombre,
                        onValueChange = onNombreChange,
                        enabled = !uiState.guardando
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileEditableField(
                        label = stringResource(R.string.last_name_label),
                        value = uiState.apellido,
                        onValueChange = onApellidoChange,
                        enabled = !uiState.guardando
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoField(
                        label = stringResource(R.string.email_label),
                        value = uiState.correo.ifBlank { "—" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileEditableField(
                        label = stringResource(R.string.phone_label),
                        value = uiState.telefono,
                        onValueChange = onTelefonoChange,
                        enabled = !uiState.guardando
                    )
                } else {
                    ProfileInfoField(
                        label = stringResource(R.string.email_label),
                        value = uiState.correo.ifBlank { "—" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoField(
                        label = stringResource(R.string.first_name_label),
                        value = uiState.nombre.ifBlank { "—" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoField(
                        label = stringResource(R.string.last_name_label),
                        value = uiState.apellido.ifBlank { "—" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoField(
                        label = stringResource(R.string.phone_label),
                        value = uiState.telefono.ifBlank { "—" }
                    )
                }

                uiState.mensajeErrorResId?.let { resId ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(resId), color = ErrorRed, fontSize = 13.sp)
                }

                uiState.mensajeExitoResId?.let { resId ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(resId), color = CyanAccent, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isEditing) {
                    Button(
                        onClick = onGuardarClick,
                        enabled = !uiState.guardando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = DarkBackground,
                            disabledContainerColor = CyanAccent.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = if (uiState.guardando) stringResource(R.string.saving_label)
                            else stringResource(R.string.profile_btn_save),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = DarkBackground
                        )
                    ) {
                        Text(
                            stringResource(R.string.profile_btn_edit),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                    ) {
                        Text(
                            stringResource(R.string.profile_btn_logout),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TextGray, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(value, fontSize = 14.sp, color = TextGray)
        }
    }
}

@Composable
fun ProfileEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                disabledContainerColor = DarkSurface,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                disabledTextColor = TextGray,
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray,
                disabledBorderColor = TextGray,
                cursorColor = CyanAccent
            )
        )
    }
}