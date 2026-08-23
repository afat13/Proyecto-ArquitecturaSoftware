package com.example.aprendeaprender.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.repository.ChallengeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChallengeViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _dailyUiState = MutableStateFlow(DailyChallengeUiState())
    val dailyUiState: StateFlow<DailyChallengeUiState> = _dailyUiState.asStateFlow()

    private val _subjectUiState = MutableStateFlow(SubjectChallengeUiState())
    val subjectUiState: StateFlow<SubjectChallengeUiState> = _subjectUiState.asStateFlow()

    private var generacionToken = 0
    private var generarPreguntaJob: Job? = null
    private var precargaCacheJob: Job? = null
    private var recargaCachePendiente = false

    private var debeAvanzarAlGenerarPregunta = false
    private var debePosponerActualAlGenerarPregunta = false

    private companion object {
        const val TAG = "ChallengeViewModel"
        const val TOTAL_RESPUESTAS_CORRECTAS_RETO = 6
        const val PREGUNTAS_A_GENERAR = 1
        const val INTENTOS_VISUALES_INICIALES = 3
    }

    fun cargarRetoDiario() {
        _dailyUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null
            )
        }

        viewModelScope.launch {
            try {
                val diasCompletados = repository.obtenerDiasCompletados()
                val retoHoy = repository.obtenerRetoHoy()
                val trophyLevel = repository.obtenerNivelTrofeo()

                _dailyUiState.update {
                    it.copy(
                        mes = repository.mesActualNombre(),
                        anio = repository.anioActual(),
                        diaActual = repository.diaActual(),
                        diasEnMes = repository.diasEnMesActual(),
                        primerDiaSemana = repository.primerDiaSemanaDelMes(),
                        diasCompletados = diasCompletados,
                        diasCompletadosCount = diasCompletados.size,
                        trophyLevel = trophyLevel,
                        retoHoyCompletado = retoHoy.completado,
                        cargando = false,
                        mensajeErrorResId = null
                    )
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Error cargando reto diario.", exception)

                _dailyUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = R.string.auth_error_generic
                    )
                }
            }
        }
    }

    fun cargarRetoPorMaterias() {
        _subjectUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null
            )
        }

        viewModelScope.launch {
            try {
                val materias = repository.obtenerMateriasInscritas()
                val retoHoy = repository.obtenerRetoHoy()
                val completadas = retoHoy.materiasCompletadas
                    .filterValues { completada -> completada }
                    .keys

                _subjectUiState.update {
                    it.copy(
                        materias = materias,
                        materiasCompletadas = completadas,
                        todasCompletadas = completadas.size >= materias.size && materias.isNotEmpty(),
                        cargando = false,
                        mensajeErrorResId = null
                    )
                }

                iniciarPrecargaCache(
                    materias = materias,
                    materiasCompletadas = completadas
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Error cargando reto por materias.", exception)

                _subjectUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = R.string.auth_error_generic
                    )
                }
            }
        }
    }

    fun iniciarPrecargaDePreguntas() {
        if (precargaCacheJob?.isActive == true) return

        precargaCacheJob = viewModelScope.launch {
            try {
                val materias = repository.obtenerMateriasInscritas()
                val retoHoy = repository.obtenerRetoHoy()

                val completadas = retoHoy.materiasCompletadas
                    .filterValues { completada -> completada }
                    .keys

                repository.precargarCacheRoundRobin(
                    materias = materias,
                    materiasCompletadas = completadas
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Error iniciando precarga de preguntas.", exception)
            }
        }
    }

    private fun iniciarPrecargaCache(
        materias: List<Subject>,
        materiasCompletadas: Set<String>
    ) {
        if (materias.isEmpty()) return

        if (precargaCacheJob?.isActive == true) {
            recargaCachePendiente = true
            return
        }

        precargaCacheJob = viewModelScope.launch {
            try {
                do {
                    recargaCachePendiente = false

                    repository.precargarCacheRoundRobin(
                        materias = materias,
                        materiasCompletadas = materiasCompletadas
                    )
                } while (recargaCachePendiente)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Error precargando caché de preguntas.", exception)
            }
        }
    }

    fun prepararMateria(subject: Subject) {
        val currentState = _subjectUiState.value

        if (currentState.materiasCompletadas.contains(subject.id)) return

        val mismaMateriaEnCurso =
            currentState.materiaActual?.id == subject.id &&
                    currentState.preguntas.isNotEmpty() &&
                    !currentState.materiaCompletada

        if (mismaMateriaEnCurso) return

        cancelarGeneracionPendiente()

        _subjectUiState.update {
            it.reiniciarParaMateria(
                subject = subject,
                cargandoActividad = false
            )
        }
    }

    fun iniciarRetoMateriaActual() {
        val state = _subjectUiState.value
        val subject = state.materiaActual ?: return

        if (state.preguntas.isNotEmpty()) return
        if (state.cargandoActividad) return
        if (state.materiaCompletada) return

        seleccionarMateria(subject)
    }

    fun seleccionarMateria(subject: Subject) {
        val currentState = _subjectUiState.value

        if (currentState.materiasCompletadas.contains(subject.id)) return

        cancelarGeneracionPendiente()

        _subjectUiState.update {
            it.reiniciarParaMateria(
                subject = subject,
                cargandoActividad = true
            )
        }

        viewModelScope.launch {
            try {
                val preguntasIniciales = repository.obtenerPreguntasGuardadasParaMateria(subject)

                if (preguntasIniciales.isEmpty()) {
                    _subjectUiState.update {
                        it.copy(
                            cargandoActividad = false,
                            generandoSiguienteLote = false,
                            mensajeErrorResId = R.string.challenge_error_questions
                        )
                    }
                    return@launch
                }

                _subjectUiState.update {
                    it.copy(
                        preguntas = preguntasIniciales,
                        indicePreguntaActual = 0,
                        respuestasCorrectas = 0,
                        respuestasIncorrectas = 0,
                        respuestasPorTiempo = 0,
                        respuestasInterrumpidas = 0,
                        totalObjetivoPreguntas = TOTAL_RESPUESTAS_CORRECTAS_RETO,
                        resultadosPreguntas = emptyList(),
                        intentosRestantes = INTENTOS_VISUALES_INICIALES,
                        respuestaSeleccionada = null,
                        mostrarResultado = false,
                        respuestaCorrecta = false,
                        tiempoAgotado = false,
                        esperandoSiguientePorTiempo = false,
                        sinIntentos = false,
                        materiaCompletada = false,
                        cargandoActividad = false,
                        generandoSiguienteLote = false,
                        mensajeErrorResId = null
                    )
                }

                generarSiguientePreguntaSiHaceFalta()

                iniciarPrecargaCache(
                    materias = listOf(subject),
                    materiasCompletadas = currentState.materiasCompletadas
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Error cargando preguntas del reto.", exception)

                _subjectUiState.update {
                    it.copy(
                        cargandoActividad = false,
                        generandoSiguienteLote = false,
                        mensajeErrorResId = R.string.challenge_error_questions
                    )
                }
            }
        }
    }

    fun seleccionarRespuesta(index: Int) {
        val state = _subjectUiState.value
        val pregunta = state.preguntaActual ?: return

        if (state.cargandoActividad) return
        if (state.mostrarResultado) return
        if (state.materiaCompletada) return

        val esCorrecta = index == pregunta.respuestaCorrecta

        _subjectUiState.update {
            it.copy(
                respuestaSeleccionada = index,
                mostrarResultado = true,
                respuestaCorrecta = esCorrecta,
                tiempoAgotado = false,
                esperandoSiguientePorTiempo = false,
                sinIntentos = false,
                mensajeErrorResId = null
            )
        }

        if (!esCorrecta) {
            generarSiguientePreguntaSiHaceFalta(forzar = true)

            state.materiaActual?.let { subject ->
                iniciarPrecargaCache(
                    materias = listOf(subject),
                    materiasCompletadas = state.materiasCompletadas
                )
            }
        }
    }

    fun reintentarPregunta() {
        val state = _subjectUiState.value

        if (state.preguntaActual == null) return
        if (state.materiaCompletada) return
        if (!state.mostrarResultado) return
        if (state.respuestaCorrecta) return

        cerrarPreguntaActualConResultado(ChallengeDotStatus.WRONG)
        avanzarASiguientePregunta()
    }

    fun continuar() {
        val state = _subjectUiState.value

        if (!state.respuestaCorrecta) return
        if (state.preguntaActual == null) return
        if (state.materiaCompletada) return

        cerrarPreguntaActualConResultado(ChallengeDotStatus.CORRECT)

        val correctas = _subjectUiState.value.respuestasCorrectas

        if (correctas >= TOTAL_RESPUESTAS_CORRECTAS_RETO) {
            completarMateriaActual()
            return
        }

        avanzarASiguientePregunta()
    }

    fun tiempoAgotadoPreguntaActual() {
        val state = _subjectUiState.value

        if (state.preguntaActual == null) return
        if (state.mostrarResultado) return
        if (state.materiaCompletada) return
        if (state.cargandoActividad) return

        cerrarPreguntaActualConResultado(ChallengeDotStatus.TIMEOUT)
        avanzarASiguientePregunta()
    }

    fun posponerPreguntaActualPorCambioModulo() {
        val state = _subjectUiState.value

        if (state.materiaActual == null) return
        if (state.preguntaActual == null) return
        if (state.materiaCompletada) return
        if (state.cargandoActividad) return
        if (state.mostrarResultado) return

        cerrarPreguntaActualConResultado(ChallengeDotStatus.INTERRUPTED)
        avanzarASiguientePregunta()
    }

    fun cerrarActividad() {
        cancelarGeneracionPendiente()
        _subjectUiState.update { SubjectChallengeUiState() }
    }

    fun resetSubjectChallenge() {
        cancelarGeneracionPendiente()
        _subjectUiState.update { SubjectChallengeUiState() }
    }

    private fun avanzarASiguientePregunta() {
        val state = _subjectUiState.value

        val haySiguientePreguntaDisponible =
            state.indicePreguntaActual < state.preguntas.lastIndex

        if (haySiguientePreguntaDisponible) {
            _subjectUiState.update {
                it.copy(
                    indicePreguntaActual = state.indicePreguntaActual + 1,
                    respuestaSeleccionada = null,
                    mostrarResultado = false,
                    respuestaCorrecta = false,
                    tiempoAgotado = false,
                    esperandoSiguientePorTiempo = false,
                    sinIntentos = false,
                    mensajeErrorResId = null
                )
            }

            generarSiguientePreguntaSiHaceFalta()
            return
        }

        _subjectUiState.update {
            it.copy(
                cargandoActividad = true,
                respuestaSeleccionada = null,
                mostrarResultado = false,
                respuestaCorrecta = false,
                tiempoAgotado = false,
                esperandoSiguientePorTiempo = true,
                sinIntentos = false,
                mensajeErrorResId = null
            )
        }

        generarSiguientePreguntaSiHaceFalta(
            forzar = true,
            avanzarAlTerminar = true
        )
    }

    private fun generarSiguientePreguntaSiHaceFalta(
        forzar: Boolean = false,
        avanzarAlTerminar: Boolean = false,
        posponerActualAlTerminar: Boolean = false
    ) {
        val state = _subjectUiState.value
        val subject = state.materiaActual ?: return
        val subjectId = subject.id
        val tokenActual = generacionToken

        if (state.materiaCompletada) return
        if (state.respuestasCorrectas >= TOTAL_RESPUESTAS_CORRECTAS_RETO) return

        if (avanzarAlTerminar) {
            debeAvanzarAlGenerarPregunta = true
        }

        if (posponerActualAlTerminar) {
            debePosponerActualAlGenerarPregunta = true
        }

        if (!forzar && hayPreguntaDisponibleDespuesDeActual(state)) return
        if (generarPreguntaJob?.isActive == true) return

        generarPreguntaJob = viewModelScope.launch {
            try {
                _subjectUiState.update {
                    it.copy(
                        generandoSiguienteLote = true,
                        mensajeErrorResId = null
                    )
                }

                val estadoActual = _subjectUiState.value

                if (!esSesionActual(subjectId, tokenActual)) return@launch
                if (estadoActual.materiaCompletada) return@launch

                val preguntasActualesDeMateria = estadoActual.preguntas.filter { pregunta ->
                    pregunta.subjectId == subjectId
                }

                val nuevasPreguntas = repository.obtenerPreguntasParaReto(
                    subject = subject,
                    cantidad = PREGUNTAS_A_GENERAR,
                    preguntasPrevias = preguntasActualesDeMateria
                ).filter { pregunta ->
                    pregunta.subjectId == subjectId
                }

                if (!esSesionActual(subjectId, tokenActual)) return@launch

                if (nuevasPreguntas.isEmpty()) {
                    _subjectUiState.update {
                        it.copy(
                            cargandoActividad = false,
                            generandoSiguienteLote = false,
                            esperandoSiguientePorTiempo = false,
                            mensajeErrorResId = R.string.challenge_error_questions
                        )
                    }
                    return@launch
                }

                _subjectUiState.update { estado ->
                    if (estado.materiaActual?.id != subjectId) {
                        estado
                    } else {
                        estado.copy(
                            preguntas = estado.preguntas + nuevasPreguntas,
                            generandoSiguienteLote = false,
                            mensajeErrorResId = null
                        )
                    }
                }

                aplicarAccionPendienteDespuesDeGenerar(
                    subjectId = subjectId,
                    token = tokenActual
                )

                if (esSesionActual(subjectId, tokenActual)) {
                    _subjectUiState.update {
                        it.copy(
                            cargandoActividad = false,
                            generandoSiguienteLote = false,
                            esperandoSiguientePorTiempo = false,
                            mensajeErrorResId = null
                        )
                    }
                }

                iniciarPrecargaCache(
                    materias = listOf(subject),
                    materiasCompletadas = _subjectUiState.value.materiasCompletadas
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Error generando pregunta.", exception)

                if (!esSesionActual(subjectId, tokenActual)) return@launch

                debeAvanzarAlGenerarPregunta = false
                debePosponerActualAlGenerarPregunta = false

                _subjectUiState.update {
                    it.copy(
                        cargandoActividad = false,
                        generandoSiguienteLote = false,
                        esperandoSiguientePorTiempo = false,
                        mensajeErrorResId = R.string.challenge_error_questions
                    )
                }
            } finally {
                generarPreguntaJob = null
            }
        }
    }

    private fun aplicarAccionPendienteDespuesDeGenerar(
        subjectId: String,
        token: Int
    ) {
        if (!esSesionActual(subjectId, token)) return

        when {
            debePosponerActualAlGenerarPregunta -> {
                debePosponerActualAlGenerarPregunta = false
                debeAvanzarAlGenerarPregunta = false
                posponerPreguntaActualPorCambioModulo()
            }

            debeAvanzarAlGenerarPregunta -> {
                debeAvanzarAlGenerarPregunta = false
                avanzarASiguientePregunta()
            }
        }
    }

    private fun completarMateriaActual() {
        val state = _subjectUiState.value
        val materia = state.materiaActual ?: return

        cancelarGeneracionPendiente()

        _subjectUiState.update {
            it.copy(
                cargandoActividad = true,
                mensajeErrorResId = null
            )
        }

        viewModelScope.launch {
            try {
                val nuevasCompletadas = state.materiasCompletadas + materia.id
                val todasCompletadasLocal =
                    nuevasCompletadas.size >= state.materias.size && state.materias.isNotEmpty()

                val todasCompletadasRemoto = repository.marcarMateriaCompletada(
                    subjectId = materia.id,
                    totalMaterias = state.materias.size
                )

                repository.guardarPreguntasTandaCompleta(
                    subjectId = materia.id,
                    preguntas = _subjectUiState.value.preguntas
                )

                repository.limpiarCacheMateria(materia.id)

                _subjectUiState.update {
                    it.copy(
                        materiasCompletadas = nuevasCompletadas,
                        todasCompletadas = todasCompletadasRemoto || todasCompletadasLocal,
                        materiaCompletada = true,
                        cargandoActividad = false,
                        generandoSiguienteLote = false,
                        esperandoSiguientePorTiempo = false,
                        mensajeErrorResId = null
                    )
                }

                cargarRetoDiario()

                iniciarPrecargaCache(
                    materias = state.materias,
                    materiasCompletadas = nuevasCompletadas
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Error completando materia.", exception)

                _subjectUiState.update {
                    it.copy(
                        cargandoActividad = false,
                        mensajeErrorResId = R.string.auth_error_generic
                    )
                }
            }
        }
    }

    private fun cerrarPreguntaActualConResultado(
        resultado: ChallengeDotStatus
    ) {
        val state = _subjectUiState.value

        if (state.preguntaActual == null) return
        if (state.materiaCompletada) return

        val nuevasCorrectas = state.respuestasCorrectas +
                if (resultado == ChallengeDotStatus.CORRECT) 1 else 0

        val nuevasIncorrectas = state.respuestasIncorrectas +
                if (resultado == ChallengeDotStatus.WRONG) 1 else 0

        val nuevasPorTiempo = state.respuestasPorTiempo +
                if (resultado == ChallengeDotStatus.TIMEOUT) 1 else 0

        val nuevasInterrumpidas = state.respuestasInterrumpidas +
                if (resultado == ChallengeDotStatus.INTERRUPTED) 1 else 0

        val nuevoTotalObjetivo = TOTAL_RESPUESTAS_CORRECTAS_RETO +
                nuevasIncorrectas +
                nuevasPorTiempo +
                nuevasInterrumpidas

        _subjectUiState.update {
            it.copy(
                respuestasCorrectas = nuevasCorrectas,
                respuestasIncorrectas = nuevasIncorrectas,
                respuestasPorTiempo = nuevasPorTiempo,
                respuestasInterrumpidas = nuevasInterrumpidas,
                totalObjetivoPreguntas = nuevoTotalObjetivo,
                resultadosPreguntas = it.resultadosPreguntas + resultado,
                respuestaSeleccionada = null,
                mostrarResultado = false,
                respuestaCorrecta = false,
                tiempoAgotado = false,
                esperandoSiguientePorTiempo = false,
                sinIntentos = false,
                mensajeErrorResId = null
            )
        }
    }

    private fun SubjectChallengeUiState.reiniciarParaMateria(
        subject: Subject,
        cargandoActividad: Boolean
    ): SubjectChallengeUiState {
        return copy(
            materiaActual = subject,
            preguntas = emptyList(),
            indicePreguntaActual = 0,
            respuestasCorrectas = 0,
            respuestasIncorrectas = 0,
            respuestasPorTiempo = 0,
            respuestasInterrumpidas = 0,
            totalObjetivoPreguntas = TOTAL_RESPUESTAS_CORRECTAS_RETO,
            resultadosPreguntas = emptyList(),
            segundosRestantes = 60,
            intentosRestantes = INTENTOS_VISUALES_INICIALES,
            respuestaSeleccionada = null,
            mostrarResultado = false,
            respuestaCorrecta = false,
            tiempoAgotado = false,
            esperandoSiguientePorTiempo = false,
            sinIntentos = false,
            materiaCompletada = false,
            cargandoActividad = cargandoActividad,
            generandoSiguienteLote = false,
            mensajeErrorResId = null
        )
    }

    private fun hayPreguntaDisponibleDespuesDeActual(
        state: SubjectChallengeUiState
    ): Boolean {
        return state.indicePreguntaActual < state.preguntas.lastIndex
    }

    private fun esSesionActual(
        subjectId: String,
        token: Int
    ): Boolean {
        val state = _subjectUiState.value

        return generacionToken == token &&
                state.materiaActual?.id == subjectId
    }

    private fun cancelarGeneracionPendiente() {
        generacionToken += 1
        generarPreguntaJob?.cancel()
        generarPreguntaJob = null
        debeAvanzarAlGenerarPregunta = false
        debePosponerActualAlGenerarPregunta = false
    }

    override fun onCleared() {
        cancelarGeneracionPendiente()
        precargaCacheJob?.cancel()
        precargaCacheJob = null
        super.onCleared()
    }
}