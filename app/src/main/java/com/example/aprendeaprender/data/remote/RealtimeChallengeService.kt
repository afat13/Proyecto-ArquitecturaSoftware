package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.DailyChallenge
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeChallengeService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {

    private fun retosRef(userId: String) =
        database.getReference("usuarios").child(userId).child("retos")

    private fun mesRef(userId: String, yearMonth: String) =
        retosRef(userId).child(yearMonth)

    private fun diaRef(userId: String, yearMonth: String, dia: String) =
        mesRef(userId, yearMonth).child(dia)

    private fun fechaParts(fecha: String): Pair<String, String> {
        val yearMonth = fecha.substring(0, 7)
        val dia = "dia_${fecha.substring(8, 10)}"
        return yearMonth to dia
    }

    suspend fun marcarMateriaCompletada(
        userId: String,
        fecha: String,
        subjectId: String,
        totalMaterias: Int
    ): Boolean {
        val (yearMonth, dia) = fechaParts(fecha)
        val diaRef = diaRef(userId, yearMonth, dia)

        diaRef.child("materiasCompletadas").child(subjectId).setValue(true).await()
        diaRef.child("fecha").setValue(fecha).await()
        diaRef.child("totalMaterias").setValue(totalMaterias).await()
        diaRef.child("timestamp").setValue(System.currentTimeMillis()).await()

        val snapshot = diaRef.child("materiasCompletadas").get().await()
        val completadas = snapshot.childrenCount.toInt()
        val todoCompletado = totalMaterias > 0 && completadas >= totalMaterias

        diaRef.child("completado").setValue(todoCompletado).await()

        return todoCompletado
    }

    suspend fun obtenerRetoDelDia(userId: String, fecha: String): DailyChallenge {
        val (yearMonth, dia) = fechaParts(fecha)
        val snapshot = diaRef(userId, yearMonth, dia).get().await()

        if (!snapshot.exists()) {
            return DailyChallenge(fecha = fecha)
        }

        val materiasMap = mutableMapOf<String, Boolean>()
        snapshot.child("materiasCompletadas").children.forEach { child ->
            materiasMap[child.key.orEmpty()] = child.getValue(Boolean::class.java) ?: false
        }

        return DailyChallenge(
            fecha = snapshot.child("fecha").getValue(String::class.java) ?: fecha,
            materiasCompletadas = materiasMap,
            totalMaterias = snapshot.child("totalMaterias").getValue(Int::class.java) ?: 0,
            completado = snapshot.child("completado").getValue(Boolean::class.java) ?: false,
            timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
        )
    }

    suspend fun obtenerDiasCompletadosDelMes(userId: String, yearMonth: String): Set<Int> {
        val snapshot = mesRef(userId, yearMonth).get().await()
        val dias = mutableSetOf<Int>()

        snapshot.children.forEach { child ->
            val completado = child.child("completado").getValue(Boolean::class.java) ?: false
            if (completado) {
                val diaStr = child.key?.removePrefix("dia_").orEmpty()
                diaStr.toIntOrNull()?.let { dias.add(it) }
            }
        }

        return dias
    }

    suspend fun contarDiasCompletadosDelMes(userId: String, yearMonth: String): Int {
        return obtenerDiasCompletadosDelMes(userId, yearMonth).size
    }

    suspend fun obtenerPreguntasDelDia(
        userId: String,
        fecha: String,
        subjectId: String
    ): List<ChallengeQuestion> {
        val (yearMonth, dia) = fechaParts(fecha)
        val snapshot = diaRef(userId, yearMonth, dia)
            .child("preguntas")
            .child(subjectId)
            .get()
            .await()

        return snapshot.children.mapNotNull { child ->
            child.getValue(ChallengeQuestion::class.java)
        }
    }

    suspend fun guardarPreguntasDelDia(
        userId: String,
        fecha: String,
        subjectId: String,
        preguntas: List<ChallengeQuestion>
    ) {
        val (yearMonth, dia) = fechaParts(fecha)
        val ref = diaRef(userId, yearMonth, dia)
            .child("preguntas")
            .child(subjectId)

        val preguntasMap = preguntas.associateBy { it.id }
        ref.setValue(preguntasMap).await()
    }
}
