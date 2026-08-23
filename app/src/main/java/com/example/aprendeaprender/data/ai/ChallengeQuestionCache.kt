package com.example.aprendeaprender.data.ai

import android.content.Context
import com.example.aprendeaprender.data.model.ChallengeQuestion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

class ChallengeQuestionCache(
    context: Context
) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val mutex = Mutex()
    private val preguntasPorClave = mutableMapOf<String, ArrayDeque<ChallengeQuestion>>()

    init {
        cargarDesdeDisco()
    }

    suspend fun cantidadDisponible(
        userId: String,
        fecha: String,
        subjectId: String
    ): Int {
        return mutex.withLock {
            preguntasPorClave[cacheKey(userId, fecha, subjectId)]?.size ?: 0
        }
    }

    suspend fun obtenerSinConsumir(
        userId: String,
        fecha: String,
        subjectId: String
    ): List<ChallengeQuestion> {
        return mutex.withLock {
            preguntasPorClave[cacheKey(userId, fecha, subjectId)]?.toList().orEmpty()
        }
    }

    suspend fun tomarPreguntas(
        userId: String,
        fecha: String,
        subjectId: String,
        cantidad: Int
    ): List<ChallengeQuestion> {
        if (cantidad <= 0) return emptyList()

        return mutex.withLock {
            val key = cacheKey(userId, fecha, subjectId)
            val cola = preguntasPorClave[key] ?: return@withLock emptyList()

            val tomadas = mutableListOf<ChallengeQuestion>()
            repeat(minOf(cantidad, cola.size)) {
                tomadas += cola.removeFirst()
            }

            if (cola.isEmpty()) {
                preguntasPorClave.remove(key)
            }

            persistirEnDisco()
            tomadas
        }
    }

    suspend fun agregarPreguntas(
        userId: String,
        fecha: String,
        subjectId: String,
        preguntas: List<ChallengeQuestion>,
        maximoPorMateria: Int
    ): Int {
        if (preguntas.isEmpty()) return 0
        if (maximoPorMateria <= 0) return 0

        return mutex.withLock {
            val key = cacheKey(userId, fecha, subjectId)
            val cola = preguntasPorClave.getOrPut(key) { ArrayDeque() }

            val existentes = cola
                .map { pregunta -> pregunta.id.ifBlank { pregunta.pregunta } }
                .toMutableSet()

            var agregadas = 0

            preguntas.forEach { pregunta ->
                if (cola.size >= maximoPorMateria) return@forEach
                if (pregunta.subjectId != subjectId) return@forEach

                val llavePregunta = pregunta.id.ifBlank { pregunta.pregunta }
                if (llavePregunta.isBlank()) return@forEach
                if (existentes.contains(llavePregunta)) return@forEach

                cola.addLast(pregunta)
                existentes.add(llavePregunta)
                agregadas += 1
            }

            if (cola.isEmpty()) {
                preguntasPorClave.remove(key)
            }

            if (agregadas > 0) {
                persistirEnDisco()
            }

            agregadas
        }
    }

    suspend fun limpiarMateria(
        userId: String,
        fecha: String,
        subjectId: String
    ) {
        mutex.withLock {
            preguntasPorClave.remove(cacheKey(userId, fecha, subjectId))
            persistirEnDisco()
        }
    }

    private fun cacheKey(
        userId: String,
        fecha: String,
        subjectId: String
    ): String {
        return "$userId|$fecha|$subjectId"
    }

    private fun cargarDesdeDisco() {
        preguntasPorClave.clear()

        val rawJson = prefs.getString(KEY_JSON, null).orEmpty()
        if (rawJson.isBlank()) return

        runCatching {
            val root = JSONObject(rawJson)
            val keys = root.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val array = root.optJSONArray(key) ?: continue
                val cola = ArrayDeque<ChallengeQuestion>()

                for (index in 0 until array.length()) {
                    val json = array.optJSONObject(index) ?: continue
                    val pregunta = json.toChallengeQuestionOrNull() ?: continue
                    cola.addLast(pregunta)
                }

                if (cola.isNotEmpty()) {
                    preguntasPorClave[key] = cola
                }
            }
        }
    }

    private fun persistirEnDisco() {
        val root = JSONObject()

        preguntasPorClave.forEach { (key, cola) ->
            val array = JSONArray()

            cola.forEach { pregunta ->
                array.put(pregunta.toJson())
            }

            root.put(key, array)
        }

        prefs.edit()
            .putString(KEY_JSON, root.toString())
            .apply()
    }

    private fun ChallengeQuestion.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("subjectId", subjectId)
            .put("subjectName", subjectName)
            .put("pregunta", pregunta)
            .put("opciones", JSONArray(opciones))
            .put("respuestaCorrecta", respuestaCorrecta)
            .put("explicacion", explicacion)
    }

    private fun JSONObject.toChallengeQuestionOrNull(): ChallengeQuestion? {
        val opcionesJson = optJSONArray("opciones") ?: return null
        val opciones = mutableListOf<String>()

        for (index in 0 until opcionesJson.length()) {
            val opcion = opcionesJson.optString(index).orEmpty()
            if (opcion.isNotBlank()) {
                opciones += opcion
            }
        }

        if (opciones.size != 4) return null

        val respuestaCorrecta = optInt("respuestaCorrecta", -1)
        if (respuestaCorrecta !in 0..3) return null

        return ChallengeQuestion(
            id = optString("id").orEmpty(),
            subjectId = optString("subjectId").orEmpty(),
            subjectName = optString("subjectName").orEmpty(),
            pregunta = optString("pregunta").orEmpty(),
            opciones = opciones,
            respuestaCorrecta = respuestaCorrecta,
            explicacion = optString("explicacion").orEmpty()
        )
    }

    private companion object {
        const val PREFS_NAME = "challenge_question_cache"
        const val KEY_JSON = "questions"
    }
}