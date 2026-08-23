package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.api.ApiService
import com.example.aprendeaprender.data.api.ParticipantRequest
import com.example.aprendeaprender.data.api.SessionStore
import com.example.aprendeaprender.data.api.SubjectRequest
import com.example.aprendeaprender.data.api.SubjectResponse
import com.example.aprendeaprender.data.api.UtadeoSubjectRequest
import com.example.aprendeaprender.data.model.Participante
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.UtadeoCourse
import java.time.OffsetDateTime

class SubjectRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun createSubject(
        asignatura: String,
        instructor: String,
        temas: List<String>
    ): String {
        return api.createSubject(
            SubjectRequest(
                name = asignatura.trim(),
                instructor = instructor.trim(),
                topics = temas.map { it.trim() }.filter { it.isNotBlank() }
            )
        ).id
    }

    suspend fun getMySubjects(): List<Subject> =
        api.getSubjects().map(::toModel)

    suspend fun deleteSubject(subjectId: String) {
        api.deleteSubject(subjectId)
    }

    suspend fun sincronizarDesdeUtadeo(
        cursos: List<UtadeoCourse>,
        participantes: Map<Int, List<Participante>>
    ) {
        api.syncUtadeoSubjects(
            cursos.map { course ->
                UtadeoSubjectRequest(
                    utadeoId = course.id,
                    name = course.fullnamedisplay.ifBlank { course.fullname },
                    instructor = course.profesor,
                    participants = participantes[course.id].orEmpty().map { participant ->
                        ParticipantRequest(
                            name = participant.nombre,
                            role = participant.rol
                        )
                    }
                )
            }
        )
    }

    suspend fun getParticipantes(subjectId: String): List<Participante> =
        api.getParticipants(subjectId).map {
            Participante(nombre = it.name, rol = it.role)
        }

    private fun toModel(response: SubjectResponse): Subject = Subject(
        id = response.id,
        userId = sessionStore.userId(),
        asignatura = response.name,
        instructor = response.instructor.orEmpty(),
        temas = response.topics,
        createdAt = response.createdAt.toMillisOrZero(),
        utadeoId = response.utadeoId
    )

    private fun String.toMillisOrZero(): Long =
        runCatching { OffsetDateTime.parse(this).toInstant().toEpochMilli() }.getOrDefault(0L)
}
