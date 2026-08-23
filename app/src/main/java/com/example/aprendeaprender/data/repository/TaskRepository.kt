package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.RealtimeTaskService

class TaskRepository(
    private val authService: FirebaseAuthService,
    private val taskService: RealtimeTaskService = RealtimeTaskService()
) {

    private fun currentUserId(): String {
        return authService.currentUser()?.uid
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }

    suspend fun createTask(
        subjectId: String,
        subjectName: String,
        titulo: String,
        descripcion: String,
        fechaEntrega: Long,
        prioridad: Task.Prioridad,
        estado: Task.Estado
    ): String {
        val task = Task(
            userId = currentUserId(),
            subjectId = subjectId,
            subjectName = subjectName,
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            fechaEntrega = fechaEntrega,
            prioridad = prioridad.name,
            estado = estado.name,
            createdAt = System.currentTimeMillis()
        )

        return taskService.createTask(task)
    }

    suspend fun getMyTasks(): List<Task> {
        return taskService.getTasksByUser(currentUserId())
    }

    suspend fun updateTaskEstado(
        subjectId: String,
        taskId: String,
        estado: Task.Estado
    ) {
        taskService.updateTaskEstado(
            userId = currentUserId(),
            subjectId = subjectId,
            taskId = taskId,
            estado = estado.name
        )
    }

    suspend fun deleteTask(
        subjectId: String,
        taskId: String
    ) {
        taskService.deleteTask(
            userId = currentUserId(),
            subjectId = subjectId,
            taskId = taskId
        )
    }

    suspend fun sincronizarTareasUtadeo(
        cursos: List<com.example.aprendeaprender.data.model.UtadeoCourse>,
        tareas: List<com.example.aprendeaprender.data.model.UtadeoAssignment>
    ) {
        taskService.sincronizarTareasUtadeo(currentUserId(), cursos, tareas)
    }
    suspend fun getTasksBySubject(subjectId: String): List<Task> {
        return taskService.getTasksByUser(currentUserId()).filter { it.subjectId == subjectId }
    }
}