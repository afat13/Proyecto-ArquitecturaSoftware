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

object ChatNotificationHelper {

    private const val CHANNEL_ID = "chat_utadeo"
    private const val CHANNEL_NAME = "Mensajes de Utadeo"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes en el chat de Utadeo"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun mostrar(
        context: Context,
        notificationId: Int,
        conversationId: Long,
        remitente: String,
        texto: String
    ) {
        crearCanal(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_conversation_id", conversationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)  // si tienes un ícono específico de chat, cámbialo
            .setContentTitle(remitente)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Sin permiso POST_NOTIFICATIONS. No pasa nada, simplemente no aparece.
            android.util.Log.w("CHAT_NOTIF", "Sin permiso de notificaciones")
        }
    }
}