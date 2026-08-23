package com.example.aprendeaprender.data.repository

import android.util.Log
import com.example.aprendeaprender.data.ai.ChallengeQuestionCache
import com.example.aprendeaprender.data.api.ApiService
import com.example.aprendeaprender.data.api.ChallengeQuestionRequest
import com.example.aprendeaprender.data.api.ChallengeQuestionResponse
import com.example.aprendeaprender.data.api.DailyChallengeResponse
import com.example.aprendeaprender.data.api.SessionStore
import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.DailyChallenge
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.SubjectChallengeData
import com.example.aprendeaprender.data.model.TrophyLevel
import com.example.aprendeaprender.data.remote.GemmaChallengeService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChallengeRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val gemmaChallengeService: GemmaChallengeService,
    private val questionCache: ChallengeQuestionCache
) {
    private companion object {
        const val TAG = "ChallengeRepository"
        const val PREGUNTAS_RETO = 6
        const val MAX_CACHE_POR_MATERIA = 10
    }

    private fun currentUserId(): String = sessionStore.userId().ifBlank {
        throw IllegalStateException("No hay usuario autenticado.")
    }

    fun fechaHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .format(Calendar.getInstance().time)

    fun yearMonthActual(): String = SimpleDateFormat("yyyy-MM", Locale.US)
        .format(Calendar.getInstance().time)

    fun mesActualNombre(): String = SimpleDateFormat("MMMM", Locale("es", "CO"))
        .format(Calendar.getInstance().time)
        .replaceFirstChar { it.uppercase() }

    fun anioActual(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun diaActual(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    fun diasEnMesActual(): Int = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    fun primerDiaSemanaDelMes(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    suspend fun obtenerMateriasInscritas(): List<Subject> = subjectRepository.getMySubjects()

    suspend fun obtenerMateriasConTareas(): List<SubjectChallengeData> {
        val subjects = subjectRepository.getMySubjects()
        val tasksBySubject = taskRepository.getMyTasks().groupBy { it.subjectId }
        return subjects.map { subject ->
            SubjectChallengeData(subject = subject, tasks = tasksBySubject[subject.id].orEmpty())
        }
    }

    suspend fun obtenerRetoHoy(): DailyChallenge = api.getTodayChallenge().toModel()

    suspend fun marcarMateriaCompletada(subjectId: String, totalMaterias: Int): Boolean =
        api.completeChallengeSubject(subjectId).completed

    suspend fun obtenerDiasCompletados(): Set<Int> =
        api.getCompletedChallengeDays(yearMonthActual()).toSet()

    suspend fun obtenerNivelTrofeo(): TrophyLevel =
        TrophyLevel.fromDays(obtenerDiasCompletados().size)

    suspend fun obtenerPreguntasGuardadasParaMateria(subject: Subject): List<ChallengeQuestion> {
        val remotas = api.getChallengeQuestions(subject.id).map(::toModel)
        if (remotas.isNotEmpty()) return remotas

        val userId = currentUserId()
        val fecha = fechaHoy()
        val preguntasCache = questionCache.tomarPreguntas(
            userId = userId,
            fecha = fecha,
            subjectId = subject.id,
            cantidad = PREGUNTAS_RETO
        )
        if (preguntasCache.isNotEmpty()) return preguntasCache

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
        if (desdeCache.size >= cantidad) return desdeCache

        val generadas = generarLotePreguntasParaMateria(
            subject = subject,
            cantidad = cantidad - desdeCache.size,
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
        val tasks = taskRepository.getTasksBySubject(subject.id)
        return gemmaChallengeService.generarPreguntas(
            subject = subject,
            tasks = tasks,
            cantidad = cantidad,
            preguntasPreviasIniciales = preguntasPrevias
        )
            .filter { it.subjectId == subject.id }
            .map { it.conOpcionesBarajadas() }
    }

    suspend fun precargarCacheRoundRobin(
        materias: List<Subject>,
        materiasCompletadas: Set<String>
    ) {
        val userId = currentUserId()
        val fecha = fechaHoy()
        val materiasValidas = materias
            .filter { it.id.isNotBlank() }
            .filterNot { materiasCompletadas.contains(it.id) }
        if (materiasValidas.isEmpty()) return

        while (true) {
            var agregoAlgunaPregunta = false
            materiasValidas.forEach { subject ->
                val disponibles = questionCache.cantidadDisponible(
                    userId = userId, fecha = fecha, subjectId = subject.id
                )
                if (disponibles >= MAX_CACHE_POR_MATERIA) return@forEach

                try {
                    val previas = questionCache.obtenerSinConsumir(
                        userId = userId, fecha = fecha, subjectId = subject.id
                    )
                    val nueva = generarLotePreguntasParaMateria(subject, 1, previas)
                    val agregadas = questionCache.agregarPreguntas(
                        userId = userId,
                        fecha = fecha,
                        subjectId = subject.id,
                        preguntas = nueva,
                        maximoPorMateria = MAX_CACHE_POR_MATERIA
                    )
                    if (agregadas > 0) agregoAlgunaPregunta = true
                } catch (exception: Exception) {
                    Log.e(TAG, "Error precargando pregunta para ${subject.asignatura}.", exception)
                }
            }

            if (!agregoAlgunaPregunta) break
            val todasLlenas = materiasValidas.all { subject ->
                questionCache.cantidadDisponible(
                    userId = userId, fecha = fecha, subjectId = subject.id
                ) >= MAX_CACHE_POR_MATERIA
            }
            if (todasLlenas) break
        }
    }

    suspend fun limpiarCacheMateria(subjectId: String) {
        questionCache.limpiarMateria(
            userId = currentUserId(), fecha = fechaHoy(), subjectId = subjectId
        )
    }

    suspend fun guardarPreguntasTandaCompleta(
        subjectId: String,
        preguntas: List<ChallengeQuestion>
    ) {
        if (preguntas.size < PREGUNTAS_RETO) return
        api.saveChallengeQuestions(
            subjectId,
            preguntas.take(PREGUNTAS_RETO).map {
                ChallengeQuestionRequest(
                    question = it.pregunta,
                    options = it.opciones,
                    correctOption = it.respuestaCorrecta,
                    explanation = it.explicacion
                )
            }
        )
    }

    private fun DailyChallengeResponse.toModel(): DailyChallenge = DailyChallenge(
        fecha = date,
        materiasCompletadas = subjects.associate { it.subjectId to it.completed },
        totalMaterias = totalSubjects,
        completado = completed,
        timestamp = System.currentTimeMillis()
    )

    private fun toModel(response: ChallengeQuestionResponse): ChallengeQuestion = ChallengeQuestion(
        id = response.id,
        subjectId = response.subjectId,
        subjectName = response.subjectName,
        pregunta = response.question,
        opciones = response.options,
        respuestaCorrecta = response.correctOption,
        explicacion = response.explanation
    )

    private fun ChallengeQuestion.conOpcionesBarajadas(): ChallengeQuestion {
        if (opciones.size != 4 || respuestaCorrecta !in opciones.indices) return this
        val opcionCorrecta = opciones[respuestaCorrecta]
        val opcionesBarajadas = opciones.shuffled()
        val nuevaRespuestaCorrecta = opcionesBarajadas.indexOf(opcionCorrecta)
        if (nuevaRespuestaCorrecta !in 0..3) return this
        return copy(opciones = opcionesBarajadas, respuestaCorrecta = nuevaRespuestaCorrecta)
    }
}
