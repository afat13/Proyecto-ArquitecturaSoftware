package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.Participante
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.UtadeoCourse
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.RealtimeSubjectService

class SubjectRepository(
    private val authService: FirebaseAuthService,
    private val subjectService: RealtimeSubjectService
) {

    private fun currentUserId(): String {
        return authService.currentUser()?.uid
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }

    suspend fun createSubject(
        asignatura: String,
        instructor: String,
        temas: List<String>
    ): String {
        val subject = Subject(
            userId = currentUserId(),
            asignatura = asignatura.trim(),
            instructor = instructor.trim(),
            temas = temas.map { it.trim() }.filter { it.isNotBlank() }
        )
        return subjectService.createSubject(subject)
    }

    suspend fun getMySubjects(): List<Subject> {
        return subjectService.getSubjectsByUser(currentUserId())
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectService.deleteSubject(currentUserId(), subjectId)
    }

    suspend fun sincronizarDesdeUtadeo(
        cursos: List<UtadeoCourse>,
        participantes: Map<Int, List<Participante>>
    ) {
        subjectService.sincronizarCursosUtadeo(currentUserId(), cursos, participantes)
    }

    suspend fun getParticipantes(subjectId: String): List<Participante> {
        return subjectService.getParticipantes(currentUserId(), subjectId)
    }
}