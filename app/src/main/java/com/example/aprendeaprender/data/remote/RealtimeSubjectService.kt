package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.Participante
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.UtadeoCourse
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeSubjectService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {

    private fun subjectsRef(userId: String) =
        database.getReference("usuarios").child(userId).child("materias")

    suspend fun createSubject(subject: Subject): String {
        val ref = subjectsRef(subject.userId).push()
        val subjectWithId = subject.copy(id = ref.key ?: "")
        ref.setValue(subjectWithId).await()
        return ref.key ?: ""
    }

    suspend fun getSubjectsByUser(userId: String): List<Subject> {
        val snapshot = subjectsRef(userId).get().await()
        return snapshot.children.mapNotNull { child ->
            Subject(
                id = child.key ?: "",
                userId = userId,
                asignatura = child.child("asignatura").getValue(String::class.java).orEmpty(),
                instructor = child.child("instructor").getValue(String::class.java).orEmpty(),
                temas = child.child("temas").children
                    .mapNotNull { it.getValue(String::class.java) },
                createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L,
                utadeoId = child.child("utadeoId").getValue(Int::class.java)
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun deleteSubject(userId: String, subjectId: String) {
        subjectsRef(userId).child(subjectId).removeValue().await()
    }

    suspend fun sincronizarCursosUtadeo(
        userId: String,
        cursos: List<UtadeoCourse>,
        participantes: Map<Int, List<Participante>>
    ) {
        cursos.forEach { curso ->
            val ref = subjectsRef(userId).child("utadeo_${curso.id}")
            val updates = mapOf<String, Any?>(
                "id" to "utadeo_${curso.id}",
                "userId" to userId,
                "asignatura" to curso.fullname,
                "instructor" to curso.profesor.ifBlank { curso.fullnamedisplay },
                "utadeoId" to curso.id,
                "createdAt" to System.currentTimeMillis()
            )
            ref.updateChildren(updates).await()

            val parts = participantes[curso.id].orEmpty()
            val partsAsMap = parts.mapIndexed { i, p ->
                i.toString().padStart(3, '0') to mapOf("nombre" to p.nombre, "rol" to p.rol)
            }.toMap()
            ref.child("participantes").setValue(partsAsMap).await()
        }
        android.util.Log.d("UTADEO_SYNC", "Sincronizados ${cursos.size} cursos + participantes (upsert)")
    }

    suspend fun getParticipantes(userId: String, subjectId: String): List<Participante> {
        val snap = subjectsRef(userId).child(subjectId).child("participantes").get().await()
        return snap.children.mapNotNull { child ->
            val nombre = child.child("nombre").getValue(String::class.java).orEmpty()
            val rol = child.child("rol").getValue(String::class.java).orEmpty()
            if (nombre.isBlank()) null else Participante(nombre, rol)
        }.sortedWith(compareBy({ it.rol != "Profesor" }, { it.nombre }))
    }
}