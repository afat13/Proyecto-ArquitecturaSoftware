package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.api.ApiService
import com.example.aprendeaprender.data.api.SessionStore
import com.example.aprendeaprender.data.api.StatusRequest
import com.example.aprendeaprender.data.api.TaskRequest
import com.example.aprendeaprender.data.api.TaskResponse
import com.example.aprendeaprender.data.api.UtadeoTaskRequest
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.data.model.UtadeoAssignment
import com.example.aprendeaprender.data.model.UtadeoCourse
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TaskRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun createTask(
        subjectId: String,
        subjectName: String,
        titulo: String,
        descripcion: String,
        fechaEntrega: Long,
        prioridad: Task.Prioridad,
        estado: Task.Estado
    ): String {
        return api.createTask(
            TaskRequest(
                subjectId = subjectId,
                title = titulo.trim(),
                description = descripcion.trim(),
                dueAt = fechaEntrega.toIsoOrNull(),
                priority = prioridad.name,
                status = estado.name
            )
        ).id
    }

    suspend fun getMyTasks(): List<Task> = api.getTasks().map(::toModel)

    suspend fun updateTaskEstado(
        subjectId: String,
        taskId: String,
        estado: Task.Estado
    ) {
        api.updateTaskStatus(taskId, StatusRequest(estado.name))
    }

    suspend fun deleteTask(
        subjectId: String,
        taskId: String
    ) {
        api.deleteTask(taskId)
    }

    suspend fun sincronizarTareasUtadeo(
        cursos: List<UtadeoCourse>,
        tareas: List<UtadeoAssignment>
    ) {
        api.syncUtadeoTasks(
            tareas.map { assignment ->
                UtadeoTaskRequest(
                    assignmentId = assignment.id,
                    courseId = assignment.courseId,
                    title = assignment.name,
                    description = assignment.descripcion,
                    dueDateMillis = assignment.dueDateMillis,
                    status = assignment.estadoEntrega
                )
            }
        )
    }

    suspend fun getTasksBySubject(subjectId: String): List<Task> =
        api.getTasks().map(::toModel).filter { it.subjectId == subjectId }

    private fun toModel(response: TaskResponse): Task = Task(
        id = response.id,
        userId = sessionStore.userId(),
        subjectId = response.subjectId,
        subjectName = response.subjectName,
        titulo = response.title,
        descripcion = response.description,
        fechaEntrega = response.dueAt?.toMillisOrZero() ?: 0L,
        prioridad = response.priority,
        estado = response.status,
        createdAt = response.createdAt.toMillisOrZero()
    )

    private fun Long.toIsoOrNull(): String? = if (this <= 0L) null else
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC).toString()

    private fun String.toMillisOrZero(): Long =
        runCatching { OffsetDateTime.parse(this).toInstant().toEpochMilli() }.getOrDefault(0L)
}
