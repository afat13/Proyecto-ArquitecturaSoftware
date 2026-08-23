package com.example.aprendeaprender.data.remote

import android.util.Log
import com.example.aprendeaprender.data.ai.GemmaModelManager
import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

class GemmaChallengeService(
    private val modelManager: GemmaModelManager
) {
    suspend fun generarPreguntas(
        subject: Subject,
        tasks: List<Task>,
        cantidad: Int = 6,
        preguntasPreviasIniciales: List<ChallengeQuestion> = emptyList()
    ): List<ChallengeQuestion> = withContext(Dispatchers.Default) {
        val preguntas = mutableListOf<ChallengeQuestion>()

        repeat(cantidad) {
            val numero = preguntasPreviasIniciales.size + preguntas.size + 1

            val pregunta = generarPreguntaConReintentos(
                subject = subject,
                tasks = tasks,
                numero = numero,
                preguntasPrevias = preguntasPreviasIniciales + preguntas
            )

            preguntas.add(pregunta)
            delay(200)
        }

        if (preguntas.size != cantidad) {
            throw IllegalStateException("No se generaron exactamente $cantidad preguntas.")
        }

        Log.d(TAG, "Lote generado correctamente: ${preguntas.size} preguntas.")
        preguntas
    }

    private suspend fun generarPreguntaConReintentos(
        subject: Subject,
        tasks: List<Task>,
        numero: Int,
        preguntasPrevias: List<ChallengeQuestion>
    ): ChallengeQuestion {
        var ultimoError: Throwable? = null

        repeat(2) { intento ->
            val prompt = buildSingleQuestionPrompt(
                subject = subject,
                tasks = tasks,
                numero = numero,
                preguntasPrevias = preguntasPrevias,
                intento = intento + 1
            )

            Log.d(TAG, "Generando pregunta $numero. Intento ${intento + 1}.")
            Log.d(TAG, "Prompt enviado:\n$prompt")

            val respuesta = modelManager.generateResponse(prompt)

            Log.d(TAG, "Respuesta cruda pregunta $numero intento ${intento + 1}:\n$respuesta")

            try {
                return parseLineFormatQuestion(
                    content = respuesta,
                    subject = subject
                )
            } catch (exception: IllegalStateException) {
                ultimoError = exception
                Log.e(TAG, "Falló el parseo de la pregunta $numero.", exception)
                delay(250)
            }
        }

        throw IllegalStateException(
            "No se pudo parsear una pregunta válida después de 2 intentos.",
            ultimoError
        )
    }

    private fun buildSingleQuestionPrompt(
        subject: Subject,
        tasks: List<Task>,
        numero: Int,
        preguntasPrevias: List<ChallengeQuestion>,
        intento: Int
    ): String {
        val temasTexto = if (subject.temas.isEmpty()) {
            "Sin temas registrados. Usa contenidos universitarios generales relacionados con la materia."
        } else {
            subject.temas.joinToString(", ")
        }

        val tareasTexto = if (tasks.isEmpty()) {
            "No hay tareas registradas. Usa principalmente el nombre de la materia y sus temas."
        } else {
            tasks.take(5).joinToString("\n") { task ->
                "- ${task.titulo.take(70)}: ${task.descripcion.ifBlank { "Sin descripción" }.take(90)}"
            }
        }

        val preguntasPreviasTexto = if (preguntasPrevias.isEmpty()) {
            "Ninguna."
        } else {
            preguntasPrevias.takeLast(6).joinToString("\n") { question ->
                "- ${question.pregunta.take(120)}"
            }
        }

        val nombreMateria = subject.asignatura.trim()

        val materiaEsIngles = nombreMateria.lowercase().let { nombre ->
            nombre.contains("ingles") ||
                    nombre.contains("inglés") ||
                    nombre.contains("english")
        }

        val instruccionIdioma = if (materiaEsIngles) {
            """
        Create ONE multiple-choice question in English.
        The QUESTION, options A, B, C, D and EXPLICACION content must be written in English.
        Focus mainly on English language structure: grammar, verb tenses, sentence structure, prepositions, connectors, modal verbs, conditionals, active/passive voice, reported speech, subject-verb agreement, vocabulary in context or reading comprehension.
        Do not ask about Spanish definitions.
        Do not ask about the history of English.
        Keep the labels exactly as: PREGUNTA, A, B, C, D, RESPUESTA, EXPLICACION.
        """.trimIndent()
        } else {
            """
        Crea UNA sola pregunta académica de selección múltiple en español.
        La pregunta, opciones y explicación deben estar en español.
        """.trimIndent()
        }

        val enfoque = if (materiaEsIngles) {
            when (numero % 6) {
                1 -> "Test correct verb tense usage in context."
                2 -> "Test sentence structure or word order."
                3 -> "Test prepositions, connectors or linking words."
                4 -> "Test modal verbs, conditionals or reported speech."
                5 -> "Test subject-verb agreement or passive voice."
                else -> "Test vocabulary or grammar through a short context."
            }
        } else {
            when (numero % 6) {
                1 -> "Aplicar un concepto a un caso breve."
                2 -> "Comparar dos enfoques sin repetir el tema anterior."
                3 -> "Inferir una consecuencia concreta."
                4 -> "Identificar un error conceptual plausible."
                5 -> "Elegir una decisión técnica o académica razonada."
                else -> "Evaluar una afirmación problemática."
            }
        }

        val instruccionTareas = if (tasks.isEmpty()) {
            if (materiaEsIngles) {
                "Use the subject name and available topics as the main source."
            } else {
                "Usa el nombre de la materia y los temas disponibles como fuente principal."
            }
        } else {
            if (materiaEsIngles) {
                "Use the student's current tasks as the main source. The question must be related to the current subject and its tasks."
            } else {
                "Usa las tareas actuales del estudiante como fuente principal. La pregunta debe estar relacionada con la materia actual y sus tareas."
            }
        }

        return """
        $instruccionIdioma
        
        Materia: $nombreMateria
        Temas disponibles: $temasTexto
        
        Contexto del estudiante:
        $tareasTexto
        
        Instrucción de enfoque:
        $instruccionTareas
        
        Preguntas ya creadas, prohibido repetir su idea central:
        $preguntasPreviasTexto
        
        Esta es la pregunta número $numero de la tanda.
        Intento: $intento.
        Enfoque obligatorio: $enfoque
        
        REGLAS DE INDEPENDENCIA:
        - No continúes la pregunta anterior.
        - No hagas referencia a preguntas anteriores.
        - No uses el mismo caso, palabra clave dominante ni estructura.
        - Cada pregunta debe poder responderse sola.
        
        DIFICULTAD:
        - Nivel universitario medio-alto.
        - No preguntes definiciones simples.
        - No preguntes datos obvios.
        - Exige análisis, aplicación, comparación o inferencia.
        - Las 3 opciones incorrectas deben ser plausibles.
        - La respuesta correcta no debe ser evidente por descarte.
        
        LONGITUD:
        - Pregunta: máximo 22 palabras.
        - Cada opción: máximo 10 palabras.
        - Explicación: máximo 18 palabras.
        - Sin párrafos largos.
        - Sin adornos ni introducciones.
        
        FORMATO OBLIGATORIO:
        - No uses JSON.
        - No uses markdown.
        - No agregues texto antes ni después.
        - Respeta exactamente las etiquetas PREGUNTA, A, B, C, D, RESPUESTA y EXPLICACION.
        - RESPUESTA debe ser solo A, B, C o D.
        
        IMPORTANTE SOBRE LA RESPUESTA CORRECTA:
        - No pongas siempre la respuesta correcta en A o B.
        - Varía naturalmente entre A, B, C y D.
        - Aun así, el código de la app barajará las opciones después del parseo.
        
        PREGUNTA: texto breve de la pregunta
        A: primera opción breve
        B: segunda opción breve
        C: tercera opción breve
        D: cuarta opción breve
        RESPUESTA: A
        EXPLICACION: justificación breve
    """.trimIndent()
    }

    private fun parseLineFormatQuestion(
        content: String,
        subject: Subject
    ): ChallengeQuestion {
        if (content.isBlank()) {
            throw IllegalStateException("Gemma devolvió una respuesta vacía.")
        }

        val clean = content
            .replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .replace("*", "")
            .trim()

        val pregunta = extractValue(clean, "PREGUNTA")
            .ifBlank { throw IllegalStateException("La respuesta no contiene PREGUNTA.") }
            .take(220)

        val opcionA = extractValue(clean, "A")
            .ifBlank { throw IllegalStateException("La respuesta no contiene opción A.") }
            .take(100)

        val opcionB = extractValue(clean, "B")
            .ifBlank { throw IllegalStateException("La respuesta no contiene opción B.") }
            .take(100)

        val opcionC = extractValue(clean, "C")
            .ifBlank { throw IllegalStateException("La respuesta no contiene opción C.") }
            .take(100)

        val opcionD = extractValue(clean, "D")
            .ifBlank { throw IllegalStateException("La respuesta no contiene opción D.") }
            .take(100)

        val opciones = listOf(opcionA, opcionB, opcionC, opcionD)

        if (opciones.distinctBy { it.lowercase().trim() }.size < 4) {
            throw IllegalStateException("Las opciones generadas están repetidas.")
        }

        val respuestaTexto = extractValue(clean, "RESPUESTA")
            .uppercase()
            .trim()

        val respuestaCorrecta = when {
            respuestaTexto.startsWith("A") -> 0
            respuestaTexto.startsWith("B") -> 1
            respuestaTexto.startsWith("C") -> 2
            respuestaTexto.startsWith("D") -> 3
            respuestaTexto.contains("0") -> 0
            respuestaTexto.contains("1") -> 1
            respuestaTexto.contains("2") -> 2
            respuestaTexto.contains("3") -> 3
            else -> throw IllegalStateException("RESPUESTA inválida: $respuestaTexto")
        }

        val explicacion = extractValue(clean, "EXPLICACION")
            .ifBlank { extractValue(clean, "EXPLICACIÓN") }
            .ifBlank { "Esta opción responde correctamente a la pregunta planteada." }
            .take(150)

        return ChallengeQuestion(
            id = "${subject.id}_${UUID.randomUUID()}",
            subjectId = subject.id,
            subjectName = subject.asignatura,
            pregunta = pregunta,
            opciones = opciones,
            respuestaCorrecta = respuestaCorrecta,
            explicacion = explicacion
        ).barajarOpciones()
    }
    private fun ChallengeQuestion.barajarOpciones(): ChallengeQuestion {
        val opcionesConRespuesta = opciones.mapIndexed { index, opcion ->
            opcion to (index == respuestaCorrecta)
        }.shuffled()

        val nuevoIndiceCorrecto = opcionesConRespuesta.indexOfFirst { it.second }

        return copy(
            opciones = opcionesConRespuesta.map { it.first },
            respuestaCorrecta = nuevoIndiceCorrecto
        )
    }
    private fun extractValue(
        text: String,
        label: String
    ): String {
        val labels = listOf(
            "PREGUNTA",
            "A",
            "B",
            "C",
            "D",
            "RESPUESTA",
            "EXPLICACION",
            "EXPLICACIÓN"
        )

        val normalizedText = text.replace("\r\n", "\n")

        val labelRegex = Regex(
            pattern = "(?im)^\\s*$label\\s*:\\s*"
        )

        val match = labelRegex.find(normalizedText) ?: return ""
        val start = match.range.last + 1

        val nextLabelStart = labels
            .filterNot { it.equals(label, ignoreCase = true) }
            .mapNotNull { nextLabel ->
                Regex("(?im)^\\s*$nextLabel\\s*:\\s*")
                    .find(normalizedText, start)
                    ?.range
                    ?.first
            }
            .filter { index -> index > start }
            .minOrNull()

        val end = nextLabelStart ?: normalizedText.length

        return normalizedText
            .substring(start, end)
            .trim()
            .trim('"')
            .trim()
    }

    private companion object {
        const val TAG = "GemmaChallenge"
    }
}
