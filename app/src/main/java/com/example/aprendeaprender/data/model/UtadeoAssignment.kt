package com.example.aprendeaprender.data.model

data class UtadeoAssignment(
    val id: Int,
    val cmid: Int,
    val courseId: Int,
    val name: String,
    val descripcion: String,
    val dueDateMillis: Long,
    val estadoEntrega: String = "PENDIENTE"  // ← nuevo: "COMPLETADA" / "EN_PROGRESO" / "PENDIENTE"
)