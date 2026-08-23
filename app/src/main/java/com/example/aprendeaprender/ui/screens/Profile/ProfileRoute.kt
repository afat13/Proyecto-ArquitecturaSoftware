package com.example.aprendeaprender.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.aprendeaprender.viewmodel.ProfileViewModel

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit = {},
    onCerrarSesionClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    ProfileScreen(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onApellidoChange = viewModel::onApellidoChange,
        onTelefonoChange = viewModel::onTelefonoChange,
        onGuardarClick = viewModel::guardarPerfil,
        onCerrarSesionClick = onCerrarSesionClick,
        onBackClick = onBackClick
    )
}