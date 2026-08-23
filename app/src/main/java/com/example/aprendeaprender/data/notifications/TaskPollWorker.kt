package com.example.aprendeaprender.data.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.aprendeaprender.data.auth.UtadeoCredentialsStore
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.RealtimeSubjectService
import com.example.aprendeaprender.data.remote.RealtimeTaskService
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.example.aprendeaprender.data.repository.UtadeoRepository
import java.util.concurrent.TimeUnit

class TaskPollWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val store = UtadeoCredentialsStore(applicationContext)
        val creds = store.obtener() ?: run {
            android.util.Log.d(TAG, "Sin credenciales, salto")
            return Result.success()
        }
        val seenStore = SeenTasksStore(applicationContext)

        return try {
            val utadeoRepo = UtadeoRepository()
            val authService = FirebaseAuthService()
            val subjectRepo = SubjectRepository(authService, RealtimeSubjectService())
            val taskRepo = TaskRepository(authService, RealtimeTaskService())

            android.util.Log.d(TAG, "Iniciando sync diario...")
            val resultado = utadeoRepo.sincronizarTodo(creds.usuario, creds.contrasena)
            android.util.Log.d(TAG, "Scrape OK: ${resultado.cursos.size} cursos, ${resultado.tareas.size} tareas")

            // Guardar en Firebase (igual que el sync manual)
            subjectRepo.sincronizarDesdeUtadeo(resultado.cursos, resultado.participantesPorCurso)
            taskRepo.sincronizarTareasUtadeo(resultado.cursos, resultado.tareas)

            // Detectar tareas nuevas
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
                android.util.Log.d(TAG, "Primera ejecución: marcadas ${resultado.tareas.size} tareas como vistas, sin notificar")
            } else {
                android.util.Log.d(TAG, "Sync diario OK. Tareas nuevas notificadas: $nuevas")
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error en sync: ${e.message}", e)
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

            val request = PeriodicWorkRequestBuilder<TaskPollWorker>(
                24, TimeUnit.HOURS
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            val oneShot = androidx.work.OneTimeWorkRequestBuilder<TaskPollWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME}_now",
                androidx.work.ExistingWorkPolicy.REPLACE,
                oneShot
            )
            android.util.Log.d(TAG, "Worker registrado (periodic 24h + one-shot inmediato)")
        }
    }
}