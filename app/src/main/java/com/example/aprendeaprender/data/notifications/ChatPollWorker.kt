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
import com.example.aprendeaprender.data.repository.ChatRepository
import java.util.concurrent.TimeUnit

class ChatPollWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val store = UtadeoCredentialsStore(applicationContext)
        val creds = store.obtener() ?: run {
            android.util.Log.d(TAG, "Sin credenciales, salto")
            return Result.success()
        }
        val seenStore = SeenMessagesStore(applicationContext)

        return try {
            val repo = ChatRepository()
            val resultado = repo.cargarBandeja(creds.usuario, creds.contrasena)

            var nuevos = 0
            resultado.conversaciones
                .filter { it.unreadCount > 0 && !it.lastMessageFromMe && it.lastMessageTime > 0 }
                .forEach { conv ->
                    // Usamos lastMessageTime como ID estable porque no tenemos el ID del último mensaje
                    val pseudoId = conv.id * 1_000_000L + (conv.lastMessageTime / 1000L)
                    if (!seenStore.yaVisto(pseudoId)) {
                        seenStore.marcarVisto(pseudoId)
                        ChatNotificationHelper.mostrar(
                            context = applicationContext,
                            notificationId = pseudoId.toInt(),
                            conversationId = conv.id,
                            remitente = conv.name,
                            texto = conv.lastMessagePreview.ifBlank { "(nuevo mensaje)" }
                        )
                        nuevos++
                    }
                }

            seenStore.limpiarSiPasaDe()
            android.util.Log.d(TAG, "Poll OK. Nuevos: $nuevos")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error en poll: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "CHAT_POLL"
        private const val WORK_NAME = "chat_poll_utadeo"

        fun registrar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Periódico cada 15 min
            val request = PeriodicWorkRequestBuilder<ChatPollWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            // Y además, dispara uno YA para no esperar 15 min
            val oneShot = androidx.work.OneTimeWorkRequestBuilder<ChatPollWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME}_now",
                androidx.work.ExistingWorkPolicy.REPLACE,
                oneShot
            )
            android.util.Log.d(TAG, "Worker registrado (periodic 15 min + one-shot inmediato)")
        }
    }
}