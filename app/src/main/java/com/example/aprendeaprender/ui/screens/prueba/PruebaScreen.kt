package com.example.aprendeaprender.ui.screens.prueba

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.data.model.UtadeoCourse
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.components.AppTextField
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.UtadeoUiState

@Composable
fun PruebaScreen(
    uiState: UtadeoUiState,
    onUsuarioChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onBuscarClick: () -> Unit,
    onResetClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var mostrarContrasena by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))


        // ── Header ─

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "login Avata",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
            if (uiState.cargado) {
                TextButton(onClick = onResetClick) {
                    Text("Cerrar sesión", color = TextGray, fontSize = 13.sp)
                }
            }
        }

        Text(text = "Cursos de Utadeo", fontSize = 14.sp, color = TextGray)

        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.cargado) {
            // ── Formulario ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ingresa tus credenciales de Utadeo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = uiState.usuario,
                    label = "Usuario Utadeo",
                    onValueChange = onUsuarioChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contraseña con ojo
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Contraseña", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.contrasena,
                        onValueChange = onContrasenaChange,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Contraseña", color = TextGray) },
                        visualTransformation = if (mostrarContrasena)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                                Icon(
                                    imageVector = if (mostrarContrasena)
                                        Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextGray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = TextGray,
                            cursorColor = CyanAccent
                        )
                    )
                }

                uiState.error?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = msg, color = ErrorRed, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = when {
                        uiState.cargando -> "Obteniendo cursos..."
                        uiState.sincronizando -> "Guardando en materias..."
                        else -> "Buscar mis cursos"
                    },
                    onClick = onBuscarClick,
                    enabled = !uiState.cargando && !uiState.sincronizando
                )

                if (uiState.cargando || uiState.sincronizando) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = CyanAccent,
                        trackColor = DarkSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.cargando)
                            "Iniciando sesión en Utadeo..."
                        else
                            "Guardando cursos en tus materias...",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        } else {
            // ── Lista de cursos ──

            // Banner de éxito
            if (uiState.materiasSincronizadas > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A2A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.materiasSincronizadas} materias y ${uiState.tareasSincronizadas} tareas guardadas",
                            color = Color(0xFF69F0AE),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "${uiState.cursos.size} cursos encontrados",
                fontSize = 13.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cursos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron cursos", color = TextGray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.cursos) { curso ->
                        CursoCard(curso = curso)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CursoCard(curso: UtadeoCourse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ID: ${curso.id}",
                    fontSize = 11.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = curso.fullname,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            if (curso.fullnamedisplay != curso.fullname && curso.fullnamedisplay.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = curso.fullnamedisplay, fontSize = 13.sp, color = TextGray)
            }
        }
    }
}