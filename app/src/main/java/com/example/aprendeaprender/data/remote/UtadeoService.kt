package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.UtadeoAssignment
import com.example.aprendeaprender.data.model.UtadeoCourse
import com.example.aprendeaprender.data.model.UtadeoConversation
import com.example.aprendeaprender.data.model.UtadeoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import com.example.aprendeaprender.data.model.Participante
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class UtadeoService {

    private val cookieJar = SessionCookieJar()

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val URL_LOGIN_PAGE = "https://www.utadeo.edu.co/es/micrositio/avata"
        const val URL_CURSOS     = "https://aulasvirtuales.utadeo.edu.co/my/courses.php"
        const val URL_AJAX       = "https://aulasvirtuales.utadeo.edu.co/lib/ajax/service.php"
        const val TAG = "UTADEO_SVC"

    }

    /** Resultado completo del scraping. */
    data class ResultadoSync(
        val cursos: List<UtadeoCourse>,
        val tareas: List<UtadeoAssignment>,
        val participantesPorCurso: Map<Int, List<Participante>>
    )

    /**
     * Flujo completo: login → cursos → profesores → tareas.
     * Devuelve todo lo necesario para sincronizar a Firebase.
     */
    suspend fun sincronizarTodo(usuario: String, contrasena: String): ResultadoSync =
        withContext(Dispatchers.IO) {
            val sesskey = login(usuario, contrasena)
            val cursosBase = obtenerCursos(sesskey)

            val cursosConProfe = mutableListOf<UtadeoCourse>()
            val tareasTotales = mutableListOf<UtadeoAssignment>()
            val participantesMap = mutableMapOf<Int, List<Participante>>()

            for (curso in cursosBase) {
                val parts = try {
                    obtenerParticipantes(curso.id)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error participantes curso=${curso.id}", e); emptyList()
                }
                participantesMap[curso.id] = parts
                cursosConProfe.add(curso.copy(profesor = extraerProfesores(parts)))

                val tareas = try {
                    obtenerTareas(curso.id)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error tareas curso=${curso.id}", e); emptyList()
                }
                tareasTotales.addAll(tareas)
            }

            ResultadoSync(cursosConProfe, tareasTotales, participantesMap)
        }

    // ─────────────────────────────────────────────────────────
    //  Pasos individuales
    // ─────────────────────────────────────────────────────────

    /** Hace login Drupal → Moodle y devuelve el sesskey. */
    private suspend fun login(usuario: String, contrasena: String): String =
        withContext(Dispatchers.IO) {
            android.util.Log.d(TAG, "PASO 1: Cargando página de login...")
            val loginPageResp = get(URL_LOGIN_PAGE)
            val loginHtml = loginPageResp.body?.string().orEmpty()
            loginPageResp.close()

            val doc = Jsoup.parse(loginHtml)
            val form = doc.selectFirst("form:has(input[name=username])")
            val formAction = form?.attr("action")?.takeIf { it.isNotBlank() }
                ?: "https://aulasvirtuales.utadeo.edu.co/login/index.php"

            val formBodyBuilder = FormBody.Builder()
                .add("username", usuario.trim())
                .add("password", contrasena)

            form?.select("input[type=hidden]")?.forEach { input ->
                val name = input.attr("name")
                val value = input.attr("value")
                if (name.isNotBlank()) formBodyBuilder.add(name, value)
            }
            form?.selectFirst("button[type=submit], input[type=submit]")?.let { btn ->
                val name = btn.attr("name")
                val value = btn.attr("value")
                if (name.isNotBlank() && value.isNotBlank()) formBodyBuilder.add(name, value)
            }

            android.util.Log.d(TAG, "PASO 2: Posteando a $formAction...")
            var currentResp = post(formAction, formBodyBuilder.build(), URL_LOGIN_PAGE)
            var redirectCount = 0
            while (currentResp.code in 301..303 && redirectCount < 15) {
                val location = currentResp.header("Location") ?: break
                val nextUrl = resolverUrl(location, currentResp.request.url.toString())
                currentResp.close()
                currentResp = get(nextUrl)
                redirectCount++
            }

            val finalBody = currentResp.body?.string().orEmpty()
            currentResp.close()

            val finalDoc = Jsoup.parse(finalBody)
            finalDoc.selectFirst(".messages--error, .alert-danger, #loginerrormessage, .loginerrors")
                ?.let { throw Exception("Credenciales incorrectas: ${it.text()}") }

            android.util.Log.d(TAG, "PASO 3: Accediendo a cursos Moodle...")
            var moodleResp = get(URL_CURSOS)
            var moodleRedirects = 0
            while (moodleResp.code in 301..303 && moodleRedirects < 15) {
                val location = moodleResp.header("Location") ?: break
                val nextUrl = resolverUrl(location, moodleResp.request.url.toString())
                moodleResp.close()
                moodleResp = get(nextUrl)
                moodleRedirects++
            }

            val cursosHtml = moodleResp.body?.string().orEmpty()
            val cursosUrl = moodleResp.request.url.toString()
            moodleResp.close()

            if (cursosUrl.contains("login/index.php") || cursosUrl.contains("login.php")) {
                throw Exception("No se pudo iniciar sesión en el aula virtual. Verifica tus credenciales.")
            }

            extraerSesskey(cursosHtml)
                ?: throw Exception("No se pudo obtener la sesión de Moodle.")
        }

    /** Lista de cursos del estudiante (AJAX Moodle). */
    private suspend fun obtenerCursos(sesskey: String): List<UtadeoCourse> =
        withContext(Dispatchers.IO) {
            val methodname = "core_course_get_enrolled_courses_by_timeline_classification"
            val payload = """[{"index":0,"methodname":"$methodname","args":{"offset":0,"limit":0,"classification":"all","sort":"fullname","customfieldname":"","customfieldvalue":""}}]"""
            val ajaxUrl = "$URL_AJAX?sesskey=$sesskey&info=$methodname"

            val resp = postJson(ajaxUrl, payload)
            val body = resp.body?.string() ?: "[]"
            resp.close()

            val array = JSONArray(body)
            if (array.length() == 0) return@withContext emptyList()
            val primer = array.getJSONObject(0)
            if (primer.optBoolean("error", false)) {
                throw Exception("Error del servidor: ${primer.optString("message", "desconocido")}")
            }
            val data = primer.optJSONObject("data") ?: return@withContext emptyList()
            val courses = data.optJSONArray("courses") ?: return@withContext emptyList()

            (0 until courses.length()).map { i ->
                val c = courses.getJSONObject(i)
                UtadeoCourse(
                    id = c.optInt("id", 0),
                    fullname = c.optString("fullname", ""),
                    fullnamedisplay = c.optString("fullnamedisplay", "")
                )
            }
        }

    /** Profesor de un curso. Si hay varios, los concatena con coma. */
    private suspend fun obtenerParticipantes(courseId: Int): List<Participante> = withContext(Dispatchers.IO) {
        val url = "https://aulasvirtuales.utadeo.edu.co/user/index.php?id=$courseId&perpage=5000"
        android.util.Log.d(TAG, "── PARTICIPANTES curso=$courseId ──  URL: $url")
        val resp = get(url)
        val html = resp.body?.string().orEmpty()
        android.util.Log.d(TAG, "  Status: ${resp.code}, len=${html.length}")
        resp.close()

        val doc = Jsoup.parse(html, url)
        val result = mutableListOf<Participante>()
        val seen = mutableSetOf<String>()
        val rolRegex = Regex("\\b(profesor[a]?(es)?|docente[s]?|teacher|editingteacher|estudiante[s]?|student)\\b", RegexOption.IGNORE_CASE)

        for (row in doc.select("tr")) {
            val nameLink = row.selectFirst("a[href*='/user/view.php']") ?: continue
            val nombre = nameLink.text().trim()
            if (nombre.isBlank() || nombre in seen) continue
            seen.add(nombre)

            val rowText = row.text()
            val rolDetectado = rolRegex.find(rowText)?.value?.lowercase() ?: ""
            val rol = when {
                rolDetectado.startsWith("profesor") || rolDetectado.startsWith("docente") ||
                        rolDetectado.contains("teacher") -> "Profesor"
                rolDetectado.startsWith("estudiante") || rolDetectado == "student" -> "Estudiante"
                else -> "Otro"
            }
            result.add(Participante(nombre, rol))
        }
        android.util.Log.d(TAG, "  Total participantes: ${result.size} (profes: ${result.count { it.rol == "Profesor" }})")
        result
    }

    private fun extraerProfesores(parts: List<Participante>): String =
        parts.filter { it.rol == "Profesor" }.joinToString(", ") { it.nombre }

    /** Todas las tareas (assignments) de los cursos dados en una sola llamada. */
    private suspend fun obtenerTareas(courseId: Int): List<UtadeoAssignment> = withContext(Dispatchers.IO) {
        val url = "https://aulasvirtuales.utadeo.edu.co/mod/assign/index.php?id=$courseId"
        android.util.Log.d(TAG, "── TAREAS (HTML) curso=$courseId ──")
        android.util.Log.d(TAG, "  URL: $url")

        val resp = get(url)
        val html = resp.body?.string().orEmpty()
        android.util.Log.d(TAG, "  Status: ${resp.code}, len=${html.length}")
        resp.close()

        val doc = Jsoup.parse(html, url)
        val basicos = mutableListOf<Pair<Int, String>>()  // (cmid, nombre)
        val seen = mutableSetOf<Int>()

        for (row in doc.select("tr:has(a[href*='/mod/assign/view.php'])")) {
            val link = row.selectFirst("a[href*='/mod/assign/view.php']") ?: continue
            val cmid = Regex("id=(\\d+)").find(link.attr("href"))?.groupValues?.get(1)?.toIntOrNull()
                ?: continue
            if (!seen.add(cmid)) continue
            val name = link.text().trim()
            if (name.isBlank()) continue
            basicos.add(cmid to name)
        }

        android.util.Log.d(TAG, "  Tareas en el índice: ${basicos.size}")

        if (basicos.isEmpty()) {
            android.util.Log.w(TAG, "  ⚠️ No se detectaron tareas. Preview HTML:")
            logLargo(html.take(2500))
            return@withContext emptyList()
        }

        // Para cada tarea, ir a su página individual a sacar fecha + estado
        val result = mutableListOf<UtadeoAssignment>()
        for ((cmid, name) in basicos) {
            val detalle = try {
                obtenerDetalleEntrega(cmid)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error detalle cmid=$cmid", e)
                DetalleEntrega("PENDIENTE", 0L)
            }
            result.add(
                UtadeoAssignment(
                    id = cmid,
                    cmid = cmid,
                    courseId = courseId,
                    name = name,
                    descripcion = "",
                    dueDateMillis = detalle.dueDateMillis,
                    estadoEntrega = detalle.estado
                )
            )
        }
        android.util.Log.d(TAG, "  ✓ Tareas con detalle: ${result.size}")
        result
    }


    // ─────────────────────────────────────────────────────────
    //  Helpers HTTP (sin cambios respecto a tu versión)
    // ─────────────────────────────────────────────────────────

    private fun resolverUrl(location: String, base: String): String = when {
        location.startsWith("http") -> location
        location.startsWith("/") -> {
            val uri = base.toHttpUrlOrNull()
            "${uri?.scheme}://${uri?.host}$location"
        }
        else -> "$base/$location"
    }

    private fun get(url: String): Response = client.newCall(
        Request.Builder()
            .url(url)
            .header("User-Agent", UA)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "es-CO,es;q=0.9")
            .build()
    ).execute()

    private fun post(url: String, body: FormBody, referer: String): Response = client.newCall(
        Request.Builder()
            .url(url).post(body)
            .header("User-Agent", UA)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "es-CO,es;q=0.9")
            .header("Referer", referer)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()
    ).execute()

    private fun postJson(url: String, json: String): Response = client.newCall(
        Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .header("User-Agent", UA)
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .header("Referer", URL_CURSOS)
            .header("X-Requested-With", "XMLHttpRequest")
            .build()
    ).execute()

    private fun extraerSesskey(html: String): String? {
        val doc = Jsoup.parse(html)
        doc.selectFirst("body[data-sesskey]")?.attr("data-sesskey")
            ?.takeIf { it.isNotBlank() }?.let { return it }
        Regex(""""sesskey"\s*:\s*"([a-zA-Z0-9]+)"""").find(html)
            ?.groupValues?.get(1)?.let { return it }
        Regex("""sesskey=([a-zA-Z0-9]{10,})""").find(html)
            ?.groupValues?.get(1)?.let { return it }
        return null
    }

    private fun logLargo(msg: String) {
        val chunk = 3500
        var i = 0
        while (i < msg.length) {
            val end = minOf(i + chunk, msg.length)
            android.util.Log.d(TAG, msg.substring(i, end))
            i = end
        }
    }
    private fun parsearFechaEspanol(textoOriginal: String): Long? {
        if (textoOriginal.isBlank()) return null

        val texto = textoOriginal.lowercase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u")

        val meses = mapOf(
            "ene" to 0, "enero" to 0,
            "feb" to 1, "febrero" to 1,
            "mar" to 2, "marzo" to 2,
            "abr" to 3, "abril" to 3,
            "may" to 4, "mayo" to 4,
            "jun" to 5, "junio" to 5,
            "jul" to 6, "julio" to 6,
            "ago" to 7, "agosto" to 7,
            "sep" to 8, "sept" to 8, "septiembre" to 8, "setiembre" to 8,
            "oct" to 9, "octubre" to 9,
            "nov" to 10, "noviembre" to 10,
            "dic" to 11, "diciembre" to 11
        )

        // Caso 1: "17 de octubre de 2025, 23:59" / "17 oct 2025"
        val reTexto = Regex("""(\d{1,2})\s+(?:de\s+)?([a-z]+)\.?\s*(?:de\s+)?(\d{4})(?:[,\s]+(\d{1,2}):(\d{2})(?:\s*(am|pm))?)?""")
        reTexto.find(texto)?.let { m ->
            val day = m.groupValues[1].toIntOrNull() ?: return@let
            val monthKey = m.groupValues[2].take(4).let { if (meses.containsKey(it)) it else m.groupValues[2].take(3) }
            val month = meses[monthKey] ?: meses[m.groupValues[2]] ?: return@let
            val year = m.groupValues[3].toIntOrNull() ?: return@let
            var hour = m.groupValues[4].toIntOrNull() ?: 23
            val minute = m.groupValues[5].toIntOrNull() ?: 59
            val ampm = m.groupValues[6]
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return calendarMillis(year, month, day, hour, minute)
        }

        // Caso 2: "17/10/2025 23:59" o "17-10-2025"
        val reNumerico = Regex("""(\d{1,2})[/\-](\d{1,2})[/\-](\d{2,4})(?:[,\s]+(\d{1,2}):(\d{2}))?""")
        reNumerico.find(texto)?.let { m ->
            val day = m.groupValues[1].toIntOrNull() ?: return@let
            val month = m.groupValues[2].toIntOrNull()?.minus(1) ?: return@let
            var year = m.groupValues[3].toIntOrNull() ?: return@let
            if (year < 100) year += 2000
            val hour = m.groupValues[4].toIntOrNull() ?: 23
            val minute = m.groupValues[5].toIntOrNull() ?: 59
            return calendarMillis(year, month, day, hour, minute)
        }

        return null
    }

    private fun calendarMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Bogota"))
        cal.set(year, month, day, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"

    private class SessionCookieJar : CookieJar {
        private val cookies = mutableListOf<Cookie>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies.removeAll { ex -> cookies.any { it.name == ex.name && it.domain == ex.domain } }
            this.cookies.addAll(cookies)
        }
        override fun loadForRequest(url: HttpUrl) = cookies.filter { it.matches(url) }
    }
    private data class DetalleEntrega(val estado: String, val dueDateMillis: Long)

    private suspend fun obtenerDetalleEntrega(cmid: Int): DetalleEntrega = withContext(Dispatchers.IO) {
        val url = "https://aulasvirtuales.utadeo.edu.co/mod/assign/view.php?id=$cmid"
        val resp = get(url)
        val html = resp.body?.string().orEmpty()
        resp.close()

        val doc = Jsoup.parse(html, url)

        fun normalizar(s: String) = s.lowercase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u")

        // ── ESTADO ── busca cualquier fila que mencione "estado de la entrega"
        var statusText = ""
        for (row in doc.select("tr")) {
            val rowText = normalizar(row.text())
            if (rowText.contains("estado de la entrega") ||
                rowText.contains("estado de envio") ||
                rowText.contains("submission status")) {
                statusText = normalizar(row.select("td").lastOrNull()?.text().orEmpty())
                break
            }
        }
        android.util.Log.d(TAG, "    cmid=$cmid statusText='$statusText'")

        val estado = when {
            statusText.isBlank() -> "PENDIENTE"
            statusText.contains("no entregado") ||
                    statusText.contains("no enviado") ||
                    statusText.contains("sin entregar") ||
                    statusText.contains("sin enviar") ||
                    statusText.contains("no hay envios") ||
                    statusText.contains("no hay entregas") ||
                    statusText.contains("no submission") ||
                    statusText.contains("not submitted") -> "PENDIENTE"

            statusText.contains("borrador") || statusText.contains("draft") -> "EN_PROGRESO"

            statusText.contains("enviado para") ||
                    statusText.contains("entregado para") ||
                    statusText.contains("submitted") ||
                    statusText.contains("calificado") ||
                    statusText.contains("graded") -> "COMPLETADA"

            Regex("\\benviado\\b").containsMatchIn(statusText) ||
                    Regex("\\bentregado\\b").containsMatchIn(statusText) -> "COMPLETADA"

            else -> "PENDIENTE"
        }

        // ── FECHA ── intento 1: busca CUALQUIER fila cuyo texto completo mencione "fecha de entrega"
        var fechaMillis = 0L
        for (row in doc.select("tr")) {
            val rowText = normalizar(row.text())
            if (!(rowText.contains("fecha de entrega") ||
                        rowText.contains("fecha limite") ||
                        rowText.contains("due date"))) continue

            // Probar <time datetime>
            row.selectFirst("time[datetime]")?.let { t ->
                runCatching {
                    fechaMillis = java.time.OffsetDateTime.parse(t.attr("datetime"))
                        .toInstant().toEpochMilli()
                }
            }
            // Probar cada celda de esa fila
            if (fechaMillis <= 0) {
                for (cell in row.select("td, th")) {
                    val cellText = cell.text()
                    val parsed = parsearFechaEspanol(cellText)
                    if (parsed != null && parsed > 0) {
                        android.util.Log.d(TAG, "    cmid=$cmid fecha en fila '$cellText' → $parsed")
                        fechaMillis = parsed
                        break
                    }
                }
            }
            if (fechaMillis > 0) break
        }

        // ── FECHA ── intento 2 (fallback agresivo): escanear TODAS las celdas
        if (fechaMillis <= 0) {
            android.util.Log.w(TAG, "    cmid=$cmid no encontré fila con label de fecha, escaneando todas las celdas...")
            for (cell in doc.select("td")) {
                val cellText = cell.text()
                val parsed = parsearFechaEspanol(cellText)
                if (parsed != null && parsed > 0) {
                    android.util.Log.d(TAG, "    cmid=$cmid fecha hallada (fallback): '$cellText' → $parsed")
                    fechaMillis = parsed
                    break
                }
            }
        }

        if (fechaMillis <= 0) {
            android.util.Log.e(TAG, "    cmid=$cmid SIN FECHA después de todos los intentos. Preview HTML:")
            logLargo(html.take(3000))
        }

        android.util.Log.d(TAG, "    cmid=$cmid → estado=$estado fechaMillis=$fechaMillis")
        DetalleEntrega(estado, fechaMillis)

    }
    private suspend fun obtenerMiUserId(): Long = withContext(Dispatchers.IO) {
        val url = "https://aulasvirtuales.utadeo.edu.co/user/profile.php"
        val resp = get(url)
        val html = resp.body?.string().orEmpty()
        val finalUrl = resp.request.url.toString()
        resp.close()

        // Después del GET, finalUrl debería ser /user/profile.php?id=XXXX
        val fromUrl = Regex("[?&]id=(\\d+)").find(finalUrl)?.groupValues?.get(1)?.toLongOrNull()
        if (fromUrl != null) return@withContext fromUrl

        // Fallback: buscar M.cfg.userId o data-userid en el HTML
        Regex("""\"userId\"\s*:\s*(\d+)""").find(html)?.groupValues?.get(1)?.toLongOrNull()?.let {
            return@withContext it
        }
        Regex("""data-userid=[\"\'](\d+)[\"\']""").find(html)?.groupValues?.get(1)?.toLongOrNull()?.let {
            return@withContext it
        }
        throw Exception("No pude obtener mi userid en Moodle")
    }

    suspend fun obtenerConversaciones(sesskey: String, miUserId: Long): List<UtadeoConversation> =
        withContext(Dispatchers.IO) {
            val methodname = "core_message_get_conversations"
            val args = JSONObject().apply {
                put("userid", miUserId)
                put("limitfrom", 0)
                put("limitnum", 50)
                put("type", JSONObject.NULL)         // todos los tipos
                put("favourites", JSONObject.NULL)
                put("mergeself", true)
            }
            val payload = JSONArray().apply {
                put(JSONObject().apply {
                    put("index", 0)
                    put("methodname", methodname)
                    put("args", args)
                })
            }.toString()
            val url = "$URL_AJAX?sesskey=$sesskey&info=$methodname"

            android.util.Log.d(TAG, "── CHAT conversaciones ──  userid=$miUserId")
            val resp = postJson(url, payload)
            val body = resp.body?.string().orEmpty()
            resp.close()

            val array = JSONArray(body)
            if (array.length() == 0) return@withContext emptyList()
            val primer = array.getJSONObject(0)
            if (primer.optBoolean("error", false)) {
                throw Exception("Error chat: ${primer.optString("exception")}")
            }
            val data = primer.optJSONObject("data") ?: return@withContext emptyList()
            val conversations = data.optJSONArray("conversations") ?: return@withContext emptyList()

            val result = mutableListOf<UtadeoConversation>()
            for (i in 0 until conversations.length()) {
                val c = conversations.getJSONObject(i)
                val type = c.optInt("type", 1)
                val members = c.optJSONArray("members") ?: JSONArray()
                val otherMember = (0 until members.length())
                    .map { members.getJSONObject(it) }
                    .firstOrNull { it.optLong("id") != miUserId } ?: members.optJSONObject(0)

                val messages = c.optJSONArray("messages") ?: JSONArray()
                val lastMsg = messages.optJSONObject(0)

                val rawName = c.optString("name").ifBlank { otherMember?.optString("fullname").orEmpty() }
                val name = when (type) {
                    3 -> "Notas personales"
                    else -> rawName.ifBlank { "Sin nombre" }
                }

                val lastText = lastMsg?.optString("text").orEmpty()
                val preview = if (lastText.isNotBlank()) Jsoup.parse(lastText).text() else ""

                result.add(
                    UtadeoConversation(
                        id = c.optLong("id"),
                        type = type,
                        name = name,
                        imageUrl = otherMember?.optString("profileimageurl").takeIf { !it.isNullOrBlank() },
                        otherUserId = otherMember?.optLong("id"),
                        isRead = c.optBoolean("isread", true),
                        unreadCount = c.optInt("unreadcount", 0).coerceAtLeast(0),
                        isFavourite = c.optBoolean("isfavourite", false),
                        lastMessagePreview = preview,
                        lastMessageTime = (lastMsg?.optLong("timecreated", 0L) ?: 0L) * 1000L,
                        lastMessageFromMe = (lastMsg?.optLong("useridfrom") == miUserId)
                    )
                )
            }

            // Ordena: no leídos primero, después por fecha desc, las notas propias al final
            result.sortedWith(
                compareByDescending<UtadeoConversation> { it.unreadCount > 0 }
                    .thenBy { it.type == 3 }   // notas al final
                    .thenByDescending { it.lastMessageTime }
            )
        }

    suspend fun obtenerMensajes(
        sesskey: String,
        miUserId: Long,
        conversationId: Long,
        limit: Int = 100
    ): List<UtadeoMessage> = withContext(Dispatchers.IO) {
        val methodname = "core_message_get_conversation_messages"
        val args = JSONObject().apply {
            put("currentuserid", miUserId)
            put("convid", conversationId)
            put("limitfrom", 0)
            put("limitnum", limit)
            put("newest", true)            // del más nuevo al más viejo (como devuelve Moodle)
        }
        val payload = JSONArray().apply {
            put(JSONObject().apply {
                put("index", 0)
                put("methodname", methodname)
                put("args", args)
            })
        }.toString()
        val url = "$URL_AJAX?sesskey=$sesskey&info=$methodname"

        val resp = postJson(url, payload)
        val body = resp.body?.string().orEmpty()
        resp.close()

        val array = JSONArray(body)
        if (array.length() == 0) return@withContext emptyList()
        val primer = array.getJSONObject(0)
        if (primer.optBoolean("error", false)) {
            throw Exception("Error mensajes: ${primer.optString("exception")}")
        }
        val data = primer.optJSONObject("data") ?: return@withContext emptyList()
        val messages = data.optJSONArray("messages") ?: return@withContext emptyList()

        val result = mutableListOf<UtadeoMessage>()
        for (i in 0 until messages.length()) {
            val m = messages.getJSONObject(i)
            val rawHtml = m.optString("text", "")
            val plain = if (rawHtml.isNotBlank()) Jsoup.parse(rawHtml).text() else ""
            result.add(
                UtadeoMessage(
                    id = m.optLong("id"),
                    conversationId = conversationId,
                    userIdFrom = m.optLong("useridfrom"),
                    textPlain = plain,
                    textHtml = rawHtml,
                    timeCreated = m.optLong("timecreated", 0L) * 1000L,
                    fromMe = m.optLong("useridfrom") == miUserId
                )
            )
        }
        // Invertir para mostrar viejos→nuevos
        result.sortedBy { it.timeCreated }
    }

    data class ChatSnapshot(
        val miUserId: Long,
        val conversaciones: List<UtadeoConversation>
    )

    suspend fun cargarBandejaChat(usuario: String, contrasena: String): ChatSnapshot =
        withContext(Dispatchers.IO) {
            val sesskey = login(usuario, contrasena)
            val miUserId = obtenerMiUserId()
            android.util.Log.d(TAG, "Mi userid Moodle: $miUserId")
            val convs = obtenerConversaciones(sesskey, miUserId)
            ChatSnapshot(miUserId, convs)
        }

    suspend fun cargarMensajesConversacion(
        usuario: String,
        contrasena: String,
        conversationId: Long
    ): Pair<Long, List<UtadeoMessage>> = withContext(Dispatchers.IO) {
        val sesskey = login(usuario, contrasena)
        val miUserId = obtenerMiUserId()
        val mensajes = obtenerMensajes(sesskey, miUserId, conversationId)

        // Marcar la conversación como leída en Moodle (no rompe si falla)
        try {
            marcarConversacionLeida(sesskey, miUserId, conversationId)
        } catch (e: Exception) {
            android.util.Log.w(TAG, "No pude marcar conv $conversationId como leída: ${e.message}")
        }

        miUserId to mensajes
    }
    suspend fun enviarMensaje(
        sesskey: String,
        conversationId: Long,
        texto: String
    ): UtadeoMessage? = withContext(Dispatchers.IO) {
        val methodname = "core_message_send_messages_to_conversation"
        val args = JSONObject().apply {
            put("conversationid", conversationId)
            put("messages", JSONArray().apply {
                put(JSONObject().apply { put("text", texto) })
            })
        }
        val payload = JSONArray().apply {
            put(JSONObject().apply {
                put("index", 0)
                put("methodname", methodname)
                put("args", args)
            })
        }.toString()
        val url = "$URL_AJAX?sesskey=$sesskey&info=$methodname"

        android.util.Log.d(TAG, "── ENVIAR conv=$conversationId ──")
        val resp = postJson(url, payload)
        val body = resp.body?.string().orEmpty()
        resp.close()

        val array = JSONArray(body)
        if (array.length() == 0) return@withContext null
        val primer = array.getJSONObject(0)
        if (primer.optBoolean("error", false)) {
            throw Exception("Error envío: ${primer.optString("exception")} / ${primer.optString("message")}")
        }
        val data = primer.optJSONArray("data") ?: return@withContext null
        if (data.length() == 0) return@withContext null
        val m = data.getJSONObject(0)

        UtadeoMessage(
            id = m.optLong("id"),
            conversationId = conversationId,
            userIdFrom = m.optLong("useridfrom"),
            textPlain = Jsoup.parse(m.optString("text", "")).text(),
            textHtml = m.optString("text", ""),
            timeCreated = m.optLong("timecreated", 0L) * 1000L,
            fromMe = true
        )
    }

    /** Login → enviar mensaje → devuelve el mensaje persistido. */
    suspend fun enviarMensajeConSesion(
        usuario: String,
        contrasena: String,
        conversationId: Long,
        texto: String
    ): UtadeoMessage? = withContext(Dispatchers.IO) {
        val sesskey = login(usuario, contrasena)
        enviarMensaje(sesskey, conversationId, texto)
    }
    suspend fun marcarConversacionLeida(
        sesskey: String,
        miUserId: Long,
        conversationId: Long
    ) = withContext(Dispatchers.IO) {
        val methodname = "core_message_mark_all_conversation_messages_as_read"
        val args = JSONObject().apply {
            put("userid", miUserId)
            put("conversationid", conversationId)
        }
        val payload = JSONArray().apply {
            put(JSONObject().apply {
                put("index", 0)
                put("methodname", methodname)
                put("args", args)
            })
        }.toString()
        val url = "$URL_AJAX?sesskey=$sesskey&info=$methodname"

        android.util.Log.d(TAG, "── MARCAR LEÍDA conv=$conversationId ──")
        val resp = postJson(url, payload)
        resp.close()
    }
}