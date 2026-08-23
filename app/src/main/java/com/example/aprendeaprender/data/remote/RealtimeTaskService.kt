package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.data.model.UtadeoAssignment
import com.example.aprendeaprender.data.model.UtadeoCourse
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeTaskService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {

    private fun materiasRef(userId: String) =
        database.getReference("usuarios")
            .child(userId)
            .child("materias")

    private fun tareasDeMateriaRef(userId: String, subjectId: String) =
        materiasRef(userId)
            .child(subjectId)
            .child("tareas")

    suspend fun createTask(task: Task): String {
        require(task.userId.isNotBlank()) { "La tarea no tiene userId." }
        require(task.subjectId.isNotBlank()) { "La tarea no tiene subjectId." }

        val ref = tareasDeMateriaRef(task.userId, task.subjectId).push()
        val taskId = ref.key ?: ""
        val taskWithId = task.copy(id = taskId)
        ref.setValue(taskWithId).await()
        return taskId
    }

    suspend fun getTasksByUser(userId: String): List<Task> {
        val snapshot = materiasRef(userId).get().await()
        return snapshot.children.flatMap { subjectSnapshot ->
            val subjectId = subjectSnapshot.key.orEmpty()
            val subjectNameFromSubject = subjectSnapshot
                .child("asignatura")
                .getValue(String::class.java)
                .orEmpty()

            subjectSnapshot.child("tareas").children.map { taskSnapshot ->
                val storedSubjectId = taskSnapshot
                    .child("subjectId")
                    .getValue(String::class.java)
                    .orEmpty()
                val storedSubjectName = taskSnapshot
                    .child("subjectName")
                    .getValue(String::class.java)
                    .orEmpty()

                Task(
                    id = taskSnapshot.child("id").getValue(String::class.java)
                        ?: taskSnapshot.key.orEmpty(),
                    userId = userId,
                    subjectId = storedSubjectId.ifBlank { subjectId },
                    subjectName = storedSubjectName.ifBlank { subjectNameFromSubject },
                    titulo = taskSnapshot.child("titulo").getValue(String::class.java).orEmpty(),
                    descripcion = taskSnapshot.child("descripcion").getValue(String::class.java).orEmpty(),
                    fechaEntrega = taskSnapshot.child("fechaEntrega").getValue(Long::class.java) ?: 0L,
                    prioridad = taskSnapshot.child("prioridad").getValue(String::class.java)
                        ?: Task.Prioridad.MEDIA.name,
                    estado = taskSnapshot.child("estado").getValue(String::class.java)
                        ?: Task.Estado.PENDIENTE.name,
                    createdAt = taskSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                )
            }
        }.sortedBy { it.fechaEntrega }
    }
    suspend fun getTasksBySubject(
        userId: String,
        subjectId: String
    ): List<Task> {
        val subjectSnapshot = materiasRef(userId)
            .child(subjectId)
            .get()
            .await()

        if (!subjectSnapshot.exists()) return emptyList()

        val subjectName = subjectSnapshot
            .child("asignatura")
            .getValue(String::class.java)
            .orEmpty()

        val tasksSnapshot = subjectSnapshot.child("tareas")

        return tasksSnapshot.children.mapNotNull { taskSnapshot ->
            val taskId = taskSnapshot.child("id")
                .getValue(String::class.java)
                ?: taskSnapshot.key
                ?: return@mapNotNull null

            val storedSubjectId = taskSnapshot.child("subjectId")
                .getValue(String::class.java)
                .orEmpty()
                .ifBlank { subjectId }

            val storedSubjectName = taskSnapshot.child("subjectName")
                .getValue(String::class.java)
                .orEmpty()
                .ifBlank { subjectName }

            val prioridadTexto = taskSnapshot.child("prioridad")
                .getValue(String::class.java)
                .orEmpty()
                .trim()
                .uppercase()

            val estadoTexto = taskSnapshot.child("estado")
                .getValue(String::class.java)
                .orEmpty()
                .trim()
                .uppercase()

            val prioridad = runCatching {
                Task.Prioridad.valueOf(prioridadTexto).name
            }.getOrDefault(Task.Prioridad.MEDIA.name)

            val estado = runCatching {
                Task.Estado.valueOf(estadoTexto).name
            }.getOrDefault(Task.Estado.PENDIENTE.name)

            Task(
                id = taskId,
                userId = userId,
                subjectId = storedSubjectId,
                subjectName = storedSubjectName,
                titulo = taskSnapshot.child("titulo")
                    .getValue(String::class.java)
                    .orEmpty(),
                descripcion = taskSnapshot.child("descripcion")
                    .getValue(String::class.java)
                    .orEmpty(),
                fechaEntrega = taskSnapshot.child("fechaEntrega")
                    .getValue(Long::class.java)
                    ?: 0L,
                prioridad = prioridad,
                estado = estado
            )
        }
    }
    suspend fun updateTaskEstado(
        userId: String,
        subjectId: String,
        taskId: String,
        estado: String
    ) {
        tareasDeMateriaRef(userId, subjectId)
            .child(taskId)
            .child("estado")
            .setValue(estado)
            .await()
    }

    suspend fun deleteTask(
        userId: String,
        subjectId: String,
        taskId: String
    ) {
        tareasDeMateriaRef(userId, subjectId)
            .child(taskId)
            .removeValue()
            .await()
    }

    suspend fun sincronizarTareasUtadeo(
        userId: String,
        cursos: List<UtadeoCourse>,
        tareas: List<UtadeoAssignment>
    ) {
        val subjectNameByCourse = cursos.associateBy({ it.id }, { it.fullname })

        tareas.forEach { a ->
            val subjectId = "utadeo_${a.courseId}"
            val taskId = "utadeo_assign_${a.id}"
            val ref = tareasDeMateriaRef(userId, subjectId).child(taskId)

            val snap = ref.get().await()
            val estadoPrev = snap.child("estado").getValue(String::class.java)
            val prioridadPrev = snap.child("prioridad").getValue(String::class.java)
            val createdAtPrev = snap.child("createdAt").getValue(Long::class.java)

            // Moodle es autoritativo para COMPLETADA; en los demás casos respeta lo del usuario.
            val estadoFinal = when {
                a.estadoEntrega == "COMPLETADA" -> Task.Estado.COMPLETADA.name
                estadoPrev != null -> estadoPrev
                a.estadoEntrega == "EN_PROGRESO" -> Task.Estado.EN_PROGRESO.name
                else -> Task.Estado.PENDIENTE.name
            }

            val updates = mutableMapOf<String, Any?>(
                "id" to taskId,
                "userId" to userId,
                "subjectId" to subjectId,
                "subjectName" to (subjectNameByCourse[a.courseId] ?: ""),
                "titulo" to a.name,
                "descripcion" to a.descripcion,
                "fechaEntrega" to a.dueDateMillis,
                "estado" to estadoFinal,
                "prioridad" to (prioridadPrev ?: calcularPrioridad(a.dueDateMillis).name),
                "createdAt" to (createdAtPrev ?: System.currentTimeMillis())
            )
            ref.updateChildren(updates).await()
        }
        android.util.Log.d("UTADEO_TASK_SYNC", "Sincronizadas ${tareas.size} tareas")
    }

    private fun calcularPrioridad(dueMillis: Long): Task.Prioridad {
        if (dueMillis <= 0) return Task.Prioridad.MEDIA
        val diasRestantes = (dueMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
        return when {
            diasRestantes < 3 -> Task.Prioridad.ALTA
            diasRestantes <= 7 -> Task.Prioridad.MEDIA
            else -> Task.Prioridad.BAJA
        }
    }
}