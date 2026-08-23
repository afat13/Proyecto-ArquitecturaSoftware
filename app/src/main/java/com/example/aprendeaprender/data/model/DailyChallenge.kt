package com.example.aprendeaprender.data.model

data class DailyChallenge(
    val fecha: String = "",
    val materiasCompletadas: Map<String, Boolean> = emptyMap(),
    val totalMaterias: Int = 0,
    val completado: Boolean = false,
    val timestamp: Long = 0L
)

data class ChallengeQuestion(
    val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val pregunta: String = "",
    val opciones: List<String> = emptyList(),
    val respuestaCorrecta: Int = 0,
    val explicacion: String = ""
)

data class SubjectChallengeData(
    val subject: Subject = Subject(),
    val tasks: List<Task> = emptyList()
)

/**
 * Se conserva para compatibilidad con el módulo anterior de retos.
 * El flujo nuevo usa ChallengeQuestion para manejar 2 preguntas por materia.
 */
data class MicroActivity(
    val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val tipo: String = "quiz",
    val pregunta: String = "",
    val opciones: List<String> = emptyList(),
    val respuestaCorrecta: Int = 0,
    val explicacion: String = "",
    val completada: Boolean = false
)

enum class TrophyLevel(val label: String, val minDays: Int) {
    BRONCE("Bronce", 0),
    PLATA("Plata", 8),
    ORO("Oro", 16),
    DIAMANTE("Diamante", 24);

    companion object {
        fun fromDays(days: Int): TrophyLevel {
            return entries.lastOrNull { days >= it.minDays } ?: BRONCE
        }
    }
}
