package com.example.aprendeaprender.data.repository

import android.util.Log
import com.example.aprendeaprender.data.ai.ChallengeQuestionCache
import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.DailyChallenge
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.SubjectChallengeData
import com.example.aprendeaprender.data.model.TrophyLevel
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.GemmaChallengeService
import com.example.aprendeaprender.data.remote.RealtimeChallengeService
import com.example.aprendeaprender.data.remote.RealtimeSubjectService
import com.example.aprendeaprender.data.remote.RealtimeTaskService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChallengeRepository(
    private val authService: FirebaseAuthService,
    private val realtimeChallengeService: RealtimeChallengeService,
    private val subjectService: RealtimeSubjectService,
    private val taskService: RealtimeTaskService,
    private val gemmaChallengeService: GemmaChallengeService,
    private val questionCache: ChallengeQuestionCache
) {

    private companion object {
        const val TAG = "ChallengeRepository"
        const val PREGUNTAS_RETO = 6
        const val MAX_CACHE_POR_MATERIA = 10
    }

    private fun currentUserId(): String {
        return authService.currentUser()?.uid
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }

    fun fechaHoy(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(Calendar.getInstance().time)
    }

    fun yearMonthActual(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US)
            .format(Calendar.getInstance().time)
    }

    fun mesActualNombre(): String {
        return SimpleDateFormat("MMMM", Locale("es", "CO"))
            .format(Calendar.getInstance().time)
            .replaceFirstChar { it.uppercase() }
    }

    fun anioActual(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun diaActual(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    fun diasEnMesActual(): Int {
        return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun primerDiaSemanaDelMes(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    suspend fun obtenerMateriasInscritas(): List<Subject> {
        return subjectService.getSubjectsByUser(currentUserId())
    }

    suspend fun obtenerMateriasConTareas(): List<SubjectChallengeData> {
        val userId = currentUserId()
        val subjects = subjectService.getSubjectsByUser(userId)
        val tasksBySubject = taskService.getTasksByUser(userId).groupBy { it.subjectId }

        return subjects.map { subject ->
            SubjectChallengeData(
                subject = subject,
                tasks = tasksBySubject[subject.id].orEmpty()
            )
        }
    }

    suspend fun obtenerRetoHoy(): DailyChallenge {
        return realtimeChallengeService.obtenerRetoDelDia(
            userId = currentUserId(),
            fecha = fechaHoy()
        )
    }

    suspend fun marcarMateriaCompletada(
        subjectId: String,
        totalMaterias: Int
    ): Boolean {
        return realtimeChallengeService.marcarMateriaCompletada(
            userId = currentUserId(),
            fecha = fechaHoy(),
            subjectId = subjectId,
            totalMaterias = totalMaterias
        )
    }

    suspend fun obtenerDiasCompletados(): Set<Int> {
        return realtimeChallengeService.obtenerDiasCompletadosDelMes(
            userId = currentUserId(),
            yearMonth = yearMonthActual()
        )
    }

    suspend fun obtenerNivelTrofeo(): TrophyLevel {
        val dias = realtimeChallengeService.contarDiasCompletadosDelMes(
            userId = currentUserId(),
            yearMonth = yearMonthActual()
        )

        return TrophyLevel.fromDays(dias)
    }

    suspend fun obtenerPreguntasGuardadasParaMateria(
        subject: Subject
    ): List<ChallengeQuestion> {
        val userId = currentUserId()
        val fecha = fechaHoy()

        val preguntasCache = questionCache.tomarPreguntas(
            userId = userId,
            fecha = fecha,
            subjectId = subject.id,
            cantidad = PREGUNTAS_RETO
        )

        if (preguntasCache.isNotEmpty()) {
            return preguntasCache
        }

        return generarLotePreguntasParaMateria(
            subject = subject,
            cantidad = 1,
            preguntasPrevias = emptyList()
        )
    }

    suspend fun obtenerPreguntasParaReto(
        subject: Subject,
        cantidad: Int,
        preguntasPrevias: List<ChallengeQuestion>
    ): List<ChallengeQuestion> {
        val userId = currentUserId()
        val fecha = fechaHoy()

        val desdeCache = questionCache.tomarPreguntas(
            userId = userId,
            fecha = fecha,
            subjectId = subject.id,
            cantidad = cantidad
        )

        if (desdeCache.size >= cantidad) {
            return desdeCache
        }

        val faltantes = cantidad - desdeCache.size

        val generadas = generarLotePreguntasParaMateria(
            subject = subject,
            cantidad = faltantes,
            preguntasPrevias = preguntasPrevias + desdeCache
        )

        return desdeCache + generadas
    }

    suspend fun generarLotePreguntasParaMateria(
        subject: Subject,
        cantidad: Int,
        preguntasPrevias: List<ChallengeQuestion>
    ): List<ChallengeQuestion> {
        if (cantidad <= 0) return emptyList()

        val userId = currentUserId()

        val tasks = taskService.getTasksBySubject(
            userId = userId,
            subjectId = subject.id
        )

        return gemmaChallengeService.generarPreguntas(
            subject = subject,
            tasks = tasks,
            cantidad = cantidad,
            preguntasPreviasIniciales = preguntasPrevias
        )
            .filter { pregunta -> pregunta.subjectId == subject.id }
            .map { pregunta -> pregunta.conOpcionesBarajadas() }
    }

    suspend fun precargarCacheRoundRobin(
        materias: List<Subject>,
        materiasCompletadas: Set<String>
    ) {
        val userId = currentUserId()
        val fecha = fechaHoy()

        val materiasValidas = materias
            .filter { subject -> subject.id.isNotBlank() }
            .filterNot { subject -> materiasCompletadas.contains(subject.id) }

        if (materiasValidas.isEmpty()) return

        while (true) {
            var agregoAlgunaPregunta = false

            materiasValidas.forEach { subject ->
                val disponibles = questionCache.cantidadDisponible(
                    userId = userId,
                    fecha = fecha,
                    subjectId = subject.id
                )

                if (disponibles >= MAX_CACHE_POR_MATERIA) {
                    return@forEach
                }

                try {
                    val preguntasPrevias = questionCache.obtenerSinConsumir(
                        userId = userId,
                        fecha = fecha,
                        subjectId = subject.id
                    )

                    val nuevaPregunta = generarLotePreguntasParaMateria(
                        subject = subject,
                        cantidad = 1,
                        preguntasPrevias = preguntasPrevias
                    )

                    val agregadas = questionCache.agregarPreguntas(
                        userId = userId,
                        fecha = fecha,
                        subjectId = subject.id,
                        preguntas = nuevaPregunta,
                        maximoPorMateria = MAX_CACHE_POR_MATERIA
                    )

                    if (agregadas > 0) {
                        agregoAlgunaPregunta = true
                    }
                } catch (exception: Exception) {
                    Log.e(
                        TAG,
                        "Error precargando pregunta para ${subject.asignatura}.",
                        exception
                    )
                }
            }

            if (!agregoAlgunaPregunta) break

            val todasLlenas = materiasValidas.all { subject ->
                questionCache.cantidadDisponible(
                    userId = userId,
                    fecha = fecha,
                    subjectId = subject.id
                ) >= MAX_CACHE_POR_MATERIA
            }

            if (todasLlenas) break
        }
    }

    suspend fun limpiarCacheMateria(subjectId: String) {
        questionCache.limpiarMateria(
            userId = currentUserId(),
            fecha = fechaHoy(),
            subjectId = subjectId
        )
    }

    suspend fun guardarPreguntasTandaCompleta(
        subjectId: String,
        preguntas: List<ChallengeQuestion>
    ) {
        if (preguntas.size < PREGUNTAS_RETO) return

        realtimeChallengeService.guardarPreguntasDelDia(
            userId = currentUserId(),
            fecha = fechaHoy(),
            subjectId = subjectId,
            preguntas = preguntas.take(PREGUNTAS_RETO)
        )
    }

    private fun ChallengeQuestion.conOpcionesBarajadas(): ChallengeQuestion {
        if (opciones.size != 4) return this
        if (respuestaCorrecta !in opciones.indices) return this

        val opcionCorrecta = opciones[respuestaCorrecta]
        val opcionesBarajadas = opciones.shuffled()
        val nuevaRespuestaCorrecta = opcionesBarajadas.indexOf(opcionCorrecta)

        if (nuevaRespuestaCorrecta !in 0..3) return this

        return copy(
            opciones = opcionesBarajadas,
            respuestaCorrecta = nuevaRespuestaCorrecta
        )
    }
}