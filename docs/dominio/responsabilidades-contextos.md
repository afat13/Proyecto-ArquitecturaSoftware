# Justificación de responsabilidades y relaciones

Se describen **módulos funcionales**, no bounded contexts desplegados independientemente.

| Relación | Dirección / intercambio | Evidencia | Regla de frontera |
|---|---|---|---|
| Android → Identidad | Registro, login y Bearer | `ApiService.kt`: `/api/auth/*` | El cliente no administra contraseñas en PostgreSQL |
| Android → Materias | Listar, crear, eliminar, sincronizar | `/api/subjects`, `/api/subjects/utadeo/sync` | La materia es propiedad del usuario autenticado |
| Android → Tareas | Listar, crear, cambiar estado, sincronizar | `/api/tasks`, `/api/tasks/utadeo/sync` | Tareas valida la asociación con materias del usuario |
| Android → Retos | Consultar reto, preguntas y registrar avance | `/api/challenges/today`, `.../complete` | Retos no debe modificar reglas de tareas |
| UTADEO → Android → Backend | Cursos, tareas y participantes normalizados | `UtadeoService.kt`, `UtadeoRepository.kt`, rutas `/utadeo/sync` | La sincronización no escribe directamente en la BD |
| Gemma local → Retos | Generación local de preguntas y guardado remoto | `GemmaChallengeService.kt`, `saveChallengeQuestions` | Gemma no tiene acceso directo a la BD |

Las flechas del mapa representan llamados o transporte de datos observables. No implican integración mediante broker, eventos publicados ni servicios autónomos.

Referencias: `docs/c4/`, `app/src/main/java/com/example/aprendeaprender/data/api/ApiService.kt`, `backend/src/main/java/com/example/aprendeaprender/api/controller/`.
