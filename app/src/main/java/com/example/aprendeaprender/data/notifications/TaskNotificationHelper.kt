package com.example.aprendeaprender.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aprendeaprender.MainActivity
import com.example.aprendeaprender.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TaskNotificationHelper {

    private const val CHANNEL_ID = "tasks_utadeo"
    private const val CHANNEL_NAME = "Tareas de Utadeo"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de tareas nuevas asignadas en Utadeo"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun mostrar(
        context: Context,
        notificationId: Int,
        nombreTarea: String,
        nombreMateria: String,
        fechaEntrega: Long
    ) {
        crearCanal(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tasks", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (fechaEntrega > 0) {
            val sdf = SimpleDateFormat("d MMM 'a las' HH:mm", Locale("es")).apply {
                timeZone = TimeZone.getTimeZone("America/Bogota")
            }
            "$nombreMateria — vence ${sdf.format(Date(fechaEntrega))}"
        } else {
            nombreMateria
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Nueva tarea: $nombreTarea")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            android.util.Log.w("TASK_NOTIF", "Sin permiso de notificaciones")
        }
    }
}