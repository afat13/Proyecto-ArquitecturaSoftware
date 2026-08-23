package com.example.aprendeaprender.data.model

data class Subject(
    val id: String = "",
    val userId: String = "",
    val asignatura: String = "",
    val instructor: String = "",
    val temas: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val utadeoId: Int? = null
)