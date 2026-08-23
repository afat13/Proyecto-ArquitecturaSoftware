package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.BuildConfig
import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class OpenRouterService(
    private val apiKeyManager: OpenRouterApiKeyManager = OpenRouterApiKeyManager(
        rawKeys = BuildConfig.APIS
            .split(",", ";", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    ),
    private val model: String = BuildConfig.OPENROUTER_MODEL,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generarPreguntas(
        subject: Subject,
        tasks: List<Task>
    ): List<ChallengeQuestion> = withContext(Dispatchers.IO) {
        if (!apiKeyManager.hasKeys()) {
            return@withContext preguntasLocales(subject, tasks)
        }

        var lastError: Exception? = null

        repeat(apiKeyManager.totalKeys()) {
            val apiKey = try {
                apiKeyManager.nextAvailableKey()
            } catch (e: NoAvailableOpenRouterKeyException) {
                lastError = e
                return@withContext preguntasLocales(subject, tasks)
            }

            try {
                apiKeyManager.markRequestStarted(apiKey)
                val response = client.newCall(buildRequest(apiKey, subject, tasks)).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.code == 429) {
                    if (responseBody.contains("daily", ignoreCase = true) || responseBody.contains("quota", ignoreCase = true)) {
                        apiKeyManager.markDailyLimitReached(apiKey)
                    } else {
                        apiKeyManager.markMinuteLimitReached(apiKey)
                    }
                    return@repeat
                }

                if (response.code == 402) {
                    apiKeyManager.markDailyLimitReached(apiKey)
                    return@repeat
                }

                if (!response.isSuccessful) {
                    throw IllegalStateException("OpenRouter HTTP ${response.code}: $responseBody")
                }

                val content = JSONObject(responseBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                val preguntas = parseQuestions(content, subject)
                if (preguntas.size >= 6) {
                    return@withContext preguntas
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        preguntasLocales(subject, tasks, lastError?.message.orEmpty())
    }

    private fun buildRequest(apiKey: String, subject: Subject, tasks: List<Task>): Request {
        val messages = JSONArray()
            .put(
                JSONObject()
                    .put("role", "system")
                    .put(
                        "content",
                        "Eres un generador de retos académicos. Devuelve solo JSON válido. No uses markdown."
                    )
            )
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("content", buildPrompt(subject, tasks))
            )

        val body = JSONObject()
            .put("model", model)
            .put("temperature", 0.3)
            .put("max_tokens", 900)
            .put("messages", messages)
            .toString()
            .toRequestBody(jsonMediaType)

        return Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://github.com/afat13/Aprende-Aprender")
            .addHeader("X-Title", "Aprende a Aprender")
            .post(body)
            .build()
    }

    private fun buildPrompt(subject: Subject, tasks: List<Task>): String {
        val tareasTexto = if (tasks.isEmpty()) {
            "La materia no tiene tareas registradas. Usa el nombre de la materia y sus temas para crear preguntas útiles."
        } else {
            tasks.joinToString(separator = "\n") { task ->
                "- Tarea: ${task.titulo}. Descripción: ${task.descripcion.ifBlank { "Sin descripción" }}. Estado: ${task.estado}."
            }
        }

        val temasTexto = if (subject.temas.isEmpty()) {
            "Sin temas registrados."
        } else {
            subject.temas.joinToString(separator = ", ")
        }

        return """
            Materia: ${subject.asignatura}
            Temas registrados: $temasTexto
            Tareas del usuario:
            $tareasTexto

            Genera exactamente 6 preguntas de selección múltiple relacionadas con las tareas y la materia.
            Cada pregunta debe tener exactamente 4 opciones.
            Solo una opción debe ser correcta.
            respuestaCorrecta debe ser el índice entero de la opción correcta: 0, 1, 2 o 3.
            Las preguntas deben evaluar comprensión, no memoria superficial.

            Devuelve únicamente este JSON válido:
            {
              "preguntas": [
                {
                  "pregunta": "Texto de la pregunta",
                  "opciones": ["Opción A", "Opción B", "Opción C", "Opción D"],
                  "respuestaCorrecta": 0,
                  "explicacion": "Explicación breve de la respuesta"
                }
              ]
            }
        """.trimIndent()
    }

    private fun parseQuestions(content: String, subject: Subject): List<ChallengeQuestion> {
        val clean = content
            .replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()

        val root = JSONObject(clean)
        val array = root.getJSONArray("preguntas")
        val questions = mutableListOf<ChallengeQuestion>()

        for (index in 0 until minOf(array.length(), 6)) {
            val item = array.getJSONObject(index)
            val opcionesJson = item.getJSONArray("opciones")
            val opciones = (0 until opcionesJson.length()).map { opcionesJson.getString(it) }

            if (opciones.size == 4) {
                questions.add(
                    ChallengeQuestion(
                        id = "${subject.id}_${UUID.randomUUID()}",
                        subjectId = subject.id,
                        subjectName = subject.asignatura,
                        pregunta = item.getString("pregunta"),
                        opciones = opciones,
                        respuestaCorrecta = item.getInt("respuestaCorrecta").coerceIn(0, 3),
                        explicacion = item.optString("explicacion")
                    )
                )
            }
        }

        return questions
    }

    private fun preguntasLocales(
        subject: Subject,
        tasks: List<Task>,
        detalle: String = ""
    ): List<ChallengeQuestion> {
        val tareaPrincipal = tasks.firstOrNull()
        val descripcion = tareaPrincipal?.descripcion?.takeIf { it.isNotBlank() } ?: subject.asignatura
        val titulo = tareaPrincipal?.titulo?.takeIf { it.isNotBlank() } ?: subject.asignatura
        val detalleSeguro = detalle.takeIf { it.isNotBlank() } ?: "No hubo respuesta disponible desde OpenRouter."

        return listOf(
            ChallengeQuestion(
                id = "${subject.id}_local_1",
                subjectId = subject.id,
                subjectName = subject.asignatura,
                pregunta = "Según tus tareas de ${subject.asignatura}, ¿qué deberías revisar primero para avanzar?",
                opciones = listOf(
                    titulo,
                    "Ignorar la tarea hasta la fecha límite",
                    "Cambiar de materia sin revisar apuntes",
                    "Eliminar la materia inscrita"
                ),
                respuestaCorrecta = 0,
                explicacion = "La pregunta usa la primera tarea registrada como referencia. $detalleSeguro"
            ),
            ChallengeQuestion(
                id = "${subject.id}_local_2",
                subjectId = subject.id,
                subjectName = subject.asignatura,
                pregunta = "¿Cuál opción está más relacionada con el contenido registrado en esta materia?",
                opciones = listOf(
                    descripcion,
                    "Un dato sin relación con la asignatura",
                    "Una acción administrativa del perfil",
                    "Una configuración de navegación"
                ),
                respuestaCorrecta = 0,
                explicacion = "La opción correcta se basa en la descripción o el nombre de la tarea registrada."
            )
        )
    }
}
