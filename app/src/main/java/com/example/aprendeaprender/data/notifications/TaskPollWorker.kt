package com.example.aprendeaprender.data.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.aprendeaprender.data.api.ApiClient
import com.example.aprendeaprender.data.api.SessionStore
import com.example.aprendeaprender.data.auth.UtadeoCredentialsStore
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.example.aprendeaprender.data.repository.UtadeoRepository
import java.util.concurrent.TimeUnit

class TaskPollWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val credentialsStore = UtadeoCredentialsStore(applicationContext)
        val creds = credentialsStore.obtener() ?: run {
            android.util.Log.d(TAG, "Sin credenciales UTADEO, se omite sincronización")
            return Result.success()
        }

        val sessionStore = SessionStore(applicationContext)
        if (!sessionStore.hasSession()) {
            android.util.Log.d(TAG, "Sin sesión del backend, se omite sincronización")
            return Result.success()
        }

        val seenStore = SeenTasksStore(applicationContext)

        return try {
            val api = ApiClient.create(sessionStore)
            val utadeoRepo = UtadeoRepository()
            val subjectRepo = SubjectRepository(api, sessionStore)
            val taskRepo = TaskRepository(api, sessionStore)

            android.util.Log.d(TAG, "Iniciando sincronización diaria...")
            val resultado = utadeoRepo.sincronizarTodo(creds.usuario, creds.contrasena)
            android.util.Log.d(TAG, "UTADEO OK: ${resultado.cursos.size} cursos, ${resultado.tareas.size} tareas")

            subjectRepo.sincronizarDesdeUtadeo(resultado.cursos, resultado.participantesPorCurso)
            taskRepo.sincronizarTareasUtadeo(resultado.cursos, resultado.tareas)

            val primeraEjecucion = !seenStore.estaInicializado()
            val cursosPorId = resultado.cursos.associateBy { it.id }
            var nuevas = 0

            for (tarea in resultado.tareas) {
                val key = "task_${tarea.id}"
                if (!seenStore.yaVista(key)) {
                    seenStore.marcarVista(key)
                    if (!primeraEjecucion) {
                        val nombreMateria = cursosPorId[tarea.courseId]?.fullname ?: "Materia"
                        TaskNotificationHelper.mostrar(
                            context = applicationContext,
                            notificationId = (2_000_000L + tarea.id).toInt(),
                            nombreTarea = tarea.name,
                            nombreMateria = nombreMateria,
                            fechaEntrega = tarea.dueDateMillis
                        )
                        nuevas++
                    }
                }
            }

            if (primeraEjecucion) {
                seenStore.marcarInicializado()
                android.util.Log.d(TAG, "Primera ejecución: ${resultado.tareas.size} tareas marcadas como vistas")
            } else {
                android.util.Log.d(TAG, "Sincronización finalizada. Tareas nuevas notificadas: $nuevas")
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error en sincronización: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "TASK_POLL"
        private const val WORK_NAME = "task_sync_utadeo"

        fun registrar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<TaskPollWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
