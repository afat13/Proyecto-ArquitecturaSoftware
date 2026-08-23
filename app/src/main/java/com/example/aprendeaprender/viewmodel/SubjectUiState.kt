package com.example.aprendeaprender.viewmodel

import androidx.annotation.StringRes
import com.example.aprendeaprender.data.model.Subject

data class CreateSubjectUiState(
    val asignatura: String = "",
    val instructor: String = "",
    val temas: List<String> = listOf(""),
    val cargando: Boolean = false,
    val materiaCreada: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)

data class SubjectListUiState(
    val subjects: List<Subject> = emptyList(),
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)