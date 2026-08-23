package com.example.aprendeaprender.viewmodel

import androidx.annotation.StringRes
import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.TrophyLevel

enum class ChallengeDotStatus {
    PENDING,
    CORRECT,
    WRONG,
    TIMEOUT,
    INTERRUPTED
}

data class DailyChallengeUiState(
    val mes: String = "",
    val anio: Int = 0,
    val diasCompletados: Set<Int> = emptySet(),
    val diaActual: Int = 0,
    val diasEnMes: Int = 30,
    val primerDiaSemana: Int = 1,
    val diasCompletadosCount: Int = 0,
    val trophyLevel: TrophyLevel = TrophyLevel.BRONCE,
    val retoHoyCompletado: Boolean = false,
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)

data class SubjectChallengeUiState(
    val materias: List<Subject> = emptyList(),
    val materiasCompletadas: Set<String> = emptySet(),
    val materiaActual: Subject? = null,
    val preguntas: List<ChallengeQuestion> = emptyList(),
    val indicePreguntaActual: Int = 0,
    val respuestasCorrectas: Int = 0,
    val respuestasIncorrectas: Int = 0,
    val respuestasPorTiempo: Int = 0,
    val respuestasInterrumpidas: Int = 0,
    val totalObjetivoPreguntas: Int = 6,
    val resultadosPreguntas: List<ChallengeDotStatus> = emptyList(),
    val segundosRestantes: Int = 60,
    val intentosRestantes: Int = 1,
    val respuestaSeleccionada: Int? = null,
    val mostrarResultado: Boolean = false,
    val respuestaCorrecta: Boolean = false,
    val tiempoAgotado: Boolean = false,
    val esperandoSiguientePorTiempo: Boolean = false,
    val sinIntentos: Boolean = false,
    val materiaCompletada: Boolean = false,
    val todasCompletadas: Boolean = false,
    val cargando: Boolean = false,
    val cargandoActividad: Boolean = false,
    val generandoSiguienteLote: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
) {
    val preguntaActual: ChallengeQuestion?
        get() = preguntas.getOrNull(indicePreguntaActual)

    val numeroPreguntaVisual: Int
        get() = resultadosPreguntas.size + 1
}
