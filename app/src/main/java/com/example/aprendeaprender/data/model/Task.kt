package com.example.aprendeaprender.data.model

data class Task(
    val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaEntrega: Long = 0L,
    val prioridad: String = Prioridad.MEDIA.name,
    val estado: String = Estado.PENDIENTE.name,
    val createdAt: Long = 0L
) {
    enum class Prioridad { ALTA, MEDIA, BAJA }
    enum class Estado { PENDIENTE, EN_PROGRESO, COMPLETADA }
}
