package com.example.aprendeaprender.data.ai



import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.aprendeaprender.BuildConfig
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class GemmaModelUiState(
    val verificando: Boolean = true,
    val descargando: Boolean = false,
    val cargando: Boolean = false,
    val listo: Boolean = false,
    val progreso: Int = 0,
    val mensaje: String = "Verificando modelo de IA...",
    val error: String? = null
)

class GemmaModelManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var engine: Engine? = null
    private var bootstrapJob: Job? = null
    private val loadMutex = Mutex()
    private val generationMutex = Mutex()

    private val _uiState = MutableStateFlow(GemmaModelUiState())
    val uiState: StateFlow<GemmaModelUiState> = _uiState.asStateFlow()

    fun modelFile(): File {
        return File(
            File(appContext.filesDir, GemmaModelConstants.MODELS_DIRECTORY),
            GemmaModelConstants.MODEL_FILE_NAME
        )
    }

    fun modelExists(): Boolean {
        val file = modelFile()
        val expectedSize = BuildConfig.GEMMA_MODEL_SIZE_BYTES

        return file.exists() &&
                file.length() > 0L &&
                (expectedSize <= 0L || file.length() == expectedSize)
    }

    fun isReady(): Boolean {
        return engine != null
    }

    fun iniciarDescargaSiHaceFalta() {
        prepararEnSegundoPlano(descargarSiNoExiste = true)
    }

    fun precargarSiExiste() {
        prepararEnSegundoPlano(descargarSiNoExiste = false)
    }

    fun prepararEnSegundoPlano(descargarSiNoExiste: Boolean = true) {
        if (bootstrapJob?.isActive == true) return

        bootstrapJob = managerScope.launch {
            prepararModelo(descargarSiNoExiste = descargarSiNoExiste)
        }
    }

    suspend fun prepararModelo(descargarSiNoExiste: Boolean = true): Boolean {
        return try {
            Log.d(TAG, "Preparando modelo Gemma 4...")
            Log.d(TAG, "Ruta esperada: ${modelFile().absolutePath}")
            Log.d(TAG, "Existe modelo: ${modelExists()}")
            Log.d(TAG, "URL configurada vacía: ${BuildConfig.GEMMA_MODEL_URL.isBlank()}")

            if (engine != null) {
                publicarModeloListo()
                return true
            }

            if (modelExists()) {
                return cargarModeloLocal()
            }

            val workExistente = obtenerUltimoWorkDelModelo()

            if (workExistente != null) {
                actualizarEstadoDescarga(workExistente)

                return when (workExistente.state) {
                    WorkInfo.State.SUCCEEDED -> cargarModeloLocal()

                    WorkInfo.State.FAILED,
                    WorkInfo.State.CANCELLED -> false

                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.BLOCKED -> esperarDescargaYCargarModelo()
                }
            }

            if (!descargarSiNoExiste) {
                publicarModeloPendiente()
                return false
            }

            iniciarDescarga(reemplazar = false)
            esperarDescargaYCargarModelo()
        } catch (exception: Exception) {
            Log.e(TAG, "Error preparando Gemma 4.", exception)

            _uiState.value = GemmaModelUiState(
                verificando = false,
                descargando = false,
                cargando = false,
                listo = false,
                progreso = 0,
                mensaje = "No se pudo preparar el modelo de IA.",
                error = exception.message ?: "Error desconocido preparando Gemma 4."
            )

            false
        }
    }

    fun reintentarDescarga() {
        close()

        bootstrapJob?.cancel()
        bootstrapJob = null

        workManager.cancelUniqueWork(GemmaModelConstants.UNIQUE_WORK_NAME)

        _uiState.value = GemmaModelUiState(
            verificando = false,
            descargando = false,
            cargando = false,
            listo = false,
            progreso = 0,
            mensaje = "Reintentando descarga del modelo...",
            error = null
        )

        iniciarDescarga(reemplazar = true)
        prepararEnSegundoPlano(descargarSiNoExiste = true)
    }

    private fun iniciarDescarga(reemplazar: Boolean) {
        val modelUrl = BuildConfig.GEMMA_MODEL_URL

        Log.d(TAG, "Solicitando descarga de Gemma 4.")
        Log.d(TAG, "GEMMA_MODEL_URL: [$modelUrl]")

        if (modelExists()) {
            managerScope.launch {
                cargarModeloLocal()
            }
            return
        }

        if (modelUrl.isBlank()) {
            _uiState.value = GemmaModelUiState(
                verificando = false,
                descargando = false,
                cargando = false,
                listo = false,
                progreso = 0,
                mensaje = "Falta configurar el modelo.",
                error = "Falta GEMMA_MODEL_URL en gradle.properties."
            )
            return
        }

        if (!modelUrl.contains(".litertlm", ignoreCase = true)) {
            _uiState.value = GemmaModelUiState(
                verificando = false,
                descargando = false,
                cargando = false,
                listo = false,
                progreso = 0,
                mensaje = "Modelo incompatible.",
                error = "Gemma 4 para LiteRT-LM debe usar un archivo .litertlm con enlace /resolve/ directo."
            )
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<GemmaModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    GemmaModelConstants.KEY_MODEL_URL to modelUrl,
                    GemmaModelConstants.KEY_MODEL_TOKEN to BuildConfig.GEMMA_MODEL_TOKEN,
                    GemmaModelConstants.KEY_EXPECTED_SHA256 to BuildConfig.GEMMA_MODEL_SHA256,
                    GemmaModelConstants.KEY_EXPECTED_SIZE_BYTES to BuildConfig.GEMMA_MODEL_SIZE_BYTES
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            GemmaModelConstants.UNIQUE_WORK_NAME,
            if (reemplazar) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request
        )

        _uiState.value = GemmaModelUiState(
            verificando = false,
            descargando = true,
            cargando = false,
            listo = false,
            progreso = _uiState.value.progreso,
            mensaje = "Descargando modelo Gemma 4...",
            error = null
        )
    }

    private suspend fun obtenerUltimoWorkDelModelo(): WorkInfo? = withContext(Dispatchers.IO) {
        val works = workManager
            .getWorkInfosForUniqueWork(GemmaModelConstants.UNIQUE_WORK_NAME)
            .get()

        works.firstOrNull { info ->
            info.state == WorkInfo.State.RUNNING ||
                    info.state == WorkInfo.State.ENQUEUED ||
                    info.state == WorkInfo.State.BLOCKED
        } ?: works.firstOrNull { info ->
            info.state == WorkInfo.State.SUCCEEDED
        } ?: works.firstOrNull { info ->
            info.state == WorkInfo.State.FAILED ||
                    info.state == WorkInfo.State.CANCELLED
        }
    }

    private suspend fun esperarDescargaYCargarModelo(): Boolean {
        while (true) {
            if (engine != null) {
                publicarModeloListo()
                return true
            }

            if (modelExists()) {
                return cargarModeloLocal()
            }

            val workInfo = obtenerUltimoWorkDelModelo()

            if (workInfo == null) {
                publicarModeloPendiente()
                return false
            }

            actualizarEstadoDescarga(workInfo)

            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    return cargarModeloLocal()
                }

                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    return false
                }

                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED -> {
                    delay(1500)
                }
            }
        }
    }

    private suspend fun cargarModeloLocal(): Boolean {
        if (engine != null) {
            publicarModeloListo()
            return true
        }

        if (!modelExists()) {
            publicarModeloPendiente()
            return false
        }

        _uiState.value = GemmaModelUiState(
            verificando = false,
            descargando = false,
            cargando = true,
            listo = false,
            progreso = 100,
            mensaje = "Cargando modelo de IA...",
            error = null
        )

        val cargado = loadIfNeededSafely()

        if (cargado) {
            publicarModeloListo()
        }

        return cargado
    }

    private fun publicarModeloListo() {
        _uiState.value = GemmaModelUiState(
            verificando = false,
            descargando = false,
            cargando = false,
            listo = true,
            progreso = 100,
            mensaje = "Modelo de IA listo.",
            error = null
        )
    }

    private fun publicarModeloPendiente() {
        _uiState.value = GemmaModelUiState(
            verificando = false,
            descargando = false,
            cargando = false,
            listo = false,
            progreso = 0,
            mensaje = "Modelo pendiente de descarga.",
            error = null
        )
    }

    fun borrarModeloLocal() {
        close()
        workManager.cancelUniqueWork(GemmaModelConstants.UNIQUE_WORK_NAME)
        val model = modelFile()
        val temp = File(
            File(appContext.filesDir, GemmaModelConstants.MODELS_DIRECTORY),
            GemmaModelConstants.TEMP_MODEL_FILE_NAME
        )

        if (model.exists()) {
            Log.d(TAG, "Borrando modelo local: ${model.absolutePath}")
            model.delete()
        }

        if (temp.exists()) {
            Log.d(TAG, "Borrando temporal local: ${temp.absolutePath}")
            temp.delete()
        }

        _uiState.value = GemmaModelUiState(
            verificando = false,
            descargando = false,
            cargando = false,
            listo = false,
            progreso = 0,
            mensaje = "Modelo local eliminado.",
            error = null
        )
    }



    private suspend fun actualizarEstadoDescarga(info: WorkInfo) = withContext(Dispatchers.IO) {
        Log.d(
            TAG,
            "Estado WorkManager: ${info.state}, progreso=${
                info.progress.getInt(GemmaModelConstants.KEY_PROGRESS, -1)
            }"
        )

        when (info.state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.BLOCKED -> {
                _uiState.value = GemmaModelUiState(
                    verificando = false,
                    descargando = true,
                    cargando = false,
                    listo = false,
                    progreso = _uiState.value.progreso,
                    mensaje = "Esperando conexión para descargar Gemma 4...",
                    error = null
                )
            }

            WorkInfo.State.RUNNING -> {
                val progress = info.progress.getInt(
                    GemmaModelConstants.KEY_PROGRESS,
                    _uiState.value.progreso
                )

                _uiState.value = GemmaModelUiState(
                    verificando = false,
                    descargando = true,
                    cargando = false,
                    listo = false,
                    progreso = progress,
                    mensaje = "Descargando modelo Gemma 4...",
                    error = null
                )
            }

            WorkInfo.State.SUCCEEDED -> {
                _uiState.value = GemmaModelUiState(
                    verificando = false,
                    descargando = false,
                    cargando = true,
                    listo = false,
                    progreso = 100,
                    mensaje = "Descarga completa. Cargando Gemma 4...",
                    error = null
                )
            }

            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED -> {
                val error = info.outputData.getString(GemmaModelConstants.KEY_ERROR)
                    ?: "No se pudo descargar Gemma 4."

                _uiState.value = GemmaModelUiState(
                    verificando = false,
                    descargando = false,
                    cargando = false,
                    listo = false,
                    progreso = _uiState.value.progreso,
                    mensaje = "Error preparando Gemma 4.",
                    error = error
                )
            }
        }
    }

    private suspend fun loadIfNeededSafely(): Boolean {
        return try {
            loadIfNeeded()
            true
        } catch (outOfMemoryError: OutOfMemoryError) {
            Log.e(TAG, "Memoria insuficiente cargando Gemma 4.", outOfMemoryError)

            _uiState.value = GemmaModelUiState(
                verificando = false,
                descargando = false,
                cargando = false,
                listo = false,
                progreso = 100,
                mensaje = "No se pudo cargar Gemma 4.",
                error = "Memoria insuficiente para cargar Gemma 4 E2B. Prueba en un celular de gama alta o usa Gemma 3 1B."
            )

            false
        } catch (exception: Exception) {
            Log.e(TAG, "Error cargando Gemma 4 con LiteRT-LM.", exception)

            _uiState.value = GemmaModelUiState(
                verificando = false,
                descargando = false,
                cargando = false,
                listo = false,
                progreso = 100,
                mensaje = "No se pudo cargar Gemma 4.",
                error = exception.message ?: "El modelo no pudo cargarse. Verifica que sea un .litertlm compatible."
            )

            false
        }
    }

    @OptIn(ExperimentalApi::class)
    suspend fun loadIfNeeded() = loadMutex.withLock {
        withContext(Dispatchers.IO) {
            if (engine != null) {
                Log.d(TAG, "Gemma 4 ya estaba cargado.")
                return@withContext
            }

            val file = modelFile()

            Log.d(TAG, "Intentando cargar Gemma 4 desde: ${file.absolutePath}")
            Log.d(TAG, "Tamaño del archivo: ${file.length()} bytes")

            if (!file.exists()) {
                throw IllegalStateException("El modelo Gemma 4 no existe en ${file.absolutePath}.")
            }

            if (file.length() <= 0L) {
                throw IllegalStateException("El archivo del modelo está vacío.")
            }

            Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
            ExperimentalFlags.enableSpeculativeDecoding = true

            val gpuConfig = EngineConfig(
                modelPath = file.absolutePath,
                backend = Backend.GPU(),
                cacheDir = appContext.cacheDir.absolutePath
            )

            try {
                Log.d(TAG, "Intentando cargar Gemma 4 con GPU...")

                val gpuEngine = Engine(gpuConfig)
                gpuEngine.initialize()
                engine = gpuEngine

                Log.d(TAG, "Gemma 4 cargado correctamente con GPU.")
            } catch (gpuException: Throwable) {
                Log.e(TAG, "No se pudo cargar Gemma 4 con GPU. Probando CPU.", gpuException)

                val cpuConfig = EngineConfig(
                    modelPath = file.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = appContext.cacheDir.absolutePath
                )

                val cpuEngine = Engine(cpuConfig)
                cpuEngine.initialize()
                engine = cpuEngine

                Log.d(TAG, "Gemma 4 cargado correctamente con CPU como fallback.")
            }
        }
    }

    suspend fun generateResponse(prompt: String): String {
        return generationMutex.withLock {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Iniciando generación con Gemma 4. Longitud prompt: ${prompt.length}")

                loadIfNeeded()

                val currentEngine = engine
                    ?: throw IllegalStateException("No fue posible cargar Gemma 4.")

                val conversationConfig = ConversationConfig(
                    systemInstruction = Contents.of(
                        "Eres un generador de retos académicos. Responde en español. Sigue exactamente el formato solicitado. No agregues texto fuera del formato."
                    ),
                    samplerConfig = SamplerConfig(
                        topK = 30,
                        topP = 0.9,
                        temperature = 0.4
                    )
                )

                val response = currentEngine.createConversation(conversationConfig).use { conversation ->
                    conversation.sendMessage(prompt).toString()
                }

                Log.d(TAG, "Generación finalizada con Gemma 4. Longitud respuesta: ${response.length}")
                Log.d(TAG, "Respuesta Gemma 4:\n$response")

                response
            }
        }
    }

    fun close() {
        try {
            engine?.close()
        } catch (exception: Exception) {
            Log.e(TAG, "Error cerrando Gemma 4.", exception)
        } finally {
            engine = null
        }
    }

    private companion object {
        const val TAG = "GemmaModel"
    }
}