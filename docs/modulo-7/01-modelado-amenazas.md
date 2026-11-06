# Módulo 7 — Modelado de amenazas y controles de seguridad

**Proyecto:** Aprende a Aprender  
**Versión revisada:** `68355bf` (`main`)  
**Fecha de revisión:** 6 de noviembre de 2026  
**Estado:** Análisis inicial con controles comprobados y pendientes

## 1. Alcance

Revisamos la seguridad de las operaciones principales de Aprende a Aprender, principalmente el registro e inicio de sesión, la consulta de tareas y materias, el acceso al perfil y la comunicación entre Android, la API y PostgreSQL. El objetivo no es asegurar que el sistema está libre de vulnerabilidades, sino identificar qué protege actualmente, qué riesgos siguen presentes y cómo podríamos comprobar las mejoras.

La arquitectura de referencia tiene tres partes: la aplicación Android, la API REST de Spring Boot y PostgreSQL 16. Android envía solicitudes HTTP a la API, y la API es la encargada de consultar y modificar los datos. Las funciones de generación local con Gemma quedan fuera de las pruebas de esta primera revisión.

## 2. Activos y fronteras de confianza

| Activo | Dónde se encuentra | Por qué se protege |
|---|---|---|
| Token de sesión | Cliente Android; hash en PostgreSQL | Permite actuar en nombre de un usuario |
| Credenciales de acceso | Contraseña enviada al login; hash en PostgreSQL | Su exposición permitiría comprometer cuentas |
| Tareas, materias y perfil | PostgreSQL, accesibles mediante la API | Contienen información privada de cada estudiante |
| Base de datos | Contenedor PostgreSQL | Su acceso directo evitaría los controles de la API |
| Disponibilidad de la API | Contenedor Spring Boot | Las fallas impiden usar las funciones respaldadas por el servidor |

**Frontera 1: Android ↔ API.** El cliente no se considera confiable por sí mismo: las solicitudes necesitan autenticación y las reglas de autorización deben cumplirse en el backend, independientemente de lo que muestre la interfaz.

**Frontera 2: API ↔ PostgreSQL.** La API accede a los datos con credenciales de la base de datos. El usuario móvil no debería acceder directamente a PostgreSQL.

**Frontera 3: equipo anfitrión ↔ contenedores.** Los puertos publicados por Docker pueden ampliar la superficie de acceso. En el entorno revisado aparecen `0.0.0.0:8080` y `0.0.0.0:5432`. Esto no prueba exposición a Internet, pero sí justifica restringir los puertos en el laboratorio.

## 3. Amenazas identificadas con STRIDE

Usamos STRIDE como guía para no concentrarnos únicamente en el inicio de sesión. La probabilidad y el impacto son una **valoración preliminar cualitativa**, no mediciones de incidentes.

| ID | Categoría STRIDE | Amenaza concreta | Evidencia y control actual | Probabilidad / impacto inicial | Tratamiento propuesto | Estado |
|---|---|---|---|---|---|---|
| S-01 | Suplantación | Uso indebido de un token de sesión obtenido de un dispositivo | `AuthService` genera tokens aleatorios y conserva su hash SHA-256; `TokenAuthenticationFilter` los interpreta | Media / Alta | Proteger almacenamiento en Android y transporte con HTTPS fuera del laboratorio | Parcial; riesgo pendiente |
| S-02 | Manipulación | Un usuario intenta modificar o eliminar tareas de otro | `TaskController` condiciona operaciones con `user_id` | Media / Alta | Crear pruebas de autorización de escritura entre dos usuarios | Control observado en código; prueba pendiente |
| S-03 | Repudio | No poder reconstruir una operación problemática | No se verificó una estrategia específica de auditoría en esta revisión | Media / Media | Definir eventos auditables sin registrar contraseñas ni tokens | Pendiente |
| S-04 | Divulgación | Interceptación de datos y tokens por HTTP sin cifrar | `AndroidManifest.xml` admite tráfico en claro y la URL local predeterminada es HTTP | Media / Alta | Restringir HTTP a desarrollo y usar HTTPS en despliegues externos | Riesgo identificado en configuración |
| S-05 | Denegación de servicio | Saturación de la API o del pool de conexiones | El experimento M6 documenta aumento de latencia al crecer la carga | Alta bajo carga probada / Media | Instrumentar latencias, tasa de errores y conexiones; definir umbrales medibles | Degradación observada; causa no demostrada |
| S-06 | Elevación de privilegios | Un estudiante consulta tareas de otra cuenta | Filtro `WHERE t.user_id = :userId` y `TaskPaginationFitnessTest` | Media / Alta | Mantener prueba automática y extenderla al resto de operaciones | Control comprobado parcialmente |
| S-07 | Divulgación / manipulación | Acceso directo a PostgreSQL desde interfaces de red del host | `docker compose ps` muestra publicación `0.0.0.0:5432` | Media / Alta | Limitar publicación a `127.0.0.1` o eliminarla cuando no se requiera | Riesgo de configuración pendiente |
| S-08 | Suplantación / fuerza bruta | Intentos repetidos de login | `AuthService` comprueba contraseñas con BCrypt; no se verificó limitación de intentos | Media / Alta | Evaluar limitación por origen/cuenta y monitoreo de intentos | Parcial; pendiente |

## 4. Controles comprobados y sus límites

### Verificación HTTP sin credenciales

En la ejecución local se obtuvieron estas respuestas:

| Ruta | HTTP observado | Lectura correcta |
|---|---:|---|
| `/actuator/health` | 200 | Consulta de salud pública |
| `/actuator/info` | 200 | Endpoint accesible |
| `/actuator/prometheus` | 403 | La solicitud fue denegada; no prueba que la instrumentación esté configurada |
| `/api/tasks` | 403 | Sin acceso anónimo a tareas |
| `/api/subjects` | 403 | Sin acceso anónimo a materias |
| `/api/profile` | 403 | Sin acceso anónimo al perfil |

### Prueba automática de tareas paginadas

Se ejecutó la clase `TaskPaginationFitnessTest` sobre el commit `68355bf`, con Gradle 8.10.2 y Java 21 dentro de Docker, contra PostgreSQL. Resultado: **`BUILD SUCCESSFUL in 1m 39s`**.

La clase cubre cuatro aspectos: aislamiento entre dos usuarios, límite y validación de parámetros de paginación, orden estable para datos sin cambios y denegación de consultas sin sesión. Utiliza MockMvc con identidades simuladas para las solicitudes autenticadas; por tanto, **no sustituye una prueba real de login y uso de tokens Bearer de extremo a extremo**. La ejecución aprobada tampoco demuestra todavía la secuencia verde–rojo–verde exigida para la fitness function.

### Controles encontrados en el código

- `SecurityConfig.java` exige autenticación para rutas distintas de registro, login, health e info.
- `AuthService.java` utiliza BCrypt para contraseñas y conserva hashes SHA-256 de tokens de sesión.
- `TaskController.java` obtiene el identificador del usuario autenticado y filtra tareas por propietario.
- `SubjectController.java` y `ProfileController.java` también aplican consultas asociadas al usuario.
- `SessionStore.kt` guarda el token con `SharedPreferences` ordinarias. Su protección frente a extracción o copias requiere revisión adicional.

## 5. Priorización de trabajo

Primero proponemos reducir la superficie de exposición de PostgreSQL en el laboratorio y documentar la diferencia entre el HTTP local y el transporte que necesitaría un despliegue real. Después debemos ampliar las pruebas de autorización entre usuarios para operaciones de escritura y comprobar el flujo de autenticación Bearer real. La observabilidad servirá para detectar degradación, pero por sí sola no constituye un mecanismo de protección.

Estas son propuestas para revisar con el equipo, no decisiones arquitectónicas ya aprobadas. Antes de modificar configuraciones conviene comprobar qué conexiones locales necesitan realmente los scripts y las herramientas del proyecto.

## 6. Evidencia y trazabilidad

- `backend/src/main/java/com/example/aprendeaprender/api/config/SecurityConfig.java`
- `backend/src/main/java/com/example/aprendeaprender/api/auth/AuthService.java`
- `backend/src/main/java/com/example/aprendeaprender/api/auth/TokenAuthenticationFilter.java`
- `backend/src/main/java/com/example/aprendeaprender/api/controller/TaskController.java`
- `backend/src/main/java/com/example/aprendeaprender/api/controller/SubjectController.java`
- `backend/src/main/java/com/example/aprendeaprender/api/controller/ProfileController.java`
- `backend/src/test/java/com/example/aprendeaprender/api/TaskPaginationFitnessTest.java`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/aprendeaprender/data/api/SessionStore.kt`
- `docker-compose.yml`
- Salida local de la comprobación HTTP y de la ejecución de la prueba facilitadas el 6 de noviembre de 2026.

**Pendiente de anexar:** archivo de log conservado, resultado de la prueba con token real y validación de controles de autorización en operaciones de escritura.
