# Modelo de subdominios — Aprende a Aprender

## Alcance
El dominio es el acompañamiento de la organización y práctica académica de un estudiante. **Subdominios** son partes del problema; **módulos funcionales** son las fronteras observables en esta implementación. No afirmamos que exista DDD completo ni microservicios.

| Subdominio | Responsabilidad y conceptos propios | No le corresponde | Evidencia del repositorio |
|---|---|---|---|
| Identidad y perfil (soporte) | Registro, autenticación, sesión, datos personales | Determinar cumplimiento de tareas | `AuthController.java`, `ProfileController.java`, `AuthRepository.kt` |
| Organización académica (principal) | Materias, temas, participantes, asociación con el estudiante | Validar credenciales UTADEO en el backend | `SubjectController.java`, `SubjectRepository.kt` |
| Gestión de tareas (principal) | Tarea, vencimiento, prioridad, estado y asociación a materia | Generar preguntas mediante IA | `TaskController.java`, `TaskRepository.kt` |
| Práctica y retos (principal) | Reto diario, preguntas, respuestas y avance por materia | Crear o modificar las materias de origen | `ChallengeController.java`, `ChallengeRepository.kt` |
| Sincronización académica (apoyo) | Lectura de cursos y entregas externas, transformación y envío de datos | Ser propietario final del catálogo local | `UtadeoService.kt`, `UtadeoRepository.kt`, endpoints `/utadeo/sync` |
| Generación de preguntas (apoyo) | Inferencia Gemma, preparación de preguntas y disponibilidad del modelo | Gestionar sesiones ni escrituras directas SQL | `GemmaChallengeService.kt`, `GemmaModelManager.kt` |

## Contraste con la estructura real
No existe un backend dividido físicamente en seis servicios: los controladores Java forman parte de un mismo proceso Spring Boot y utilizan `JdbcClient` sobre PostgreSQL. En Android hay repositorios por responsabilidad. Por ello las fronteras propuestas son **lógicas** y deben conservar las dependencias existentes.

## Vocabulario de dominio
Materia: agrupador académico; tarea: compromiso académico con un estado; reto: sesión diaria de práctica; pregunta: unidad de evaluación; sincronización: importación de datos externos. Un estado de tarea no es el estado de un reto.

## Riesgo actual
Las consultas de materias, tareas y retos comparten persistencia en el backend. Esa proximidad simplifica transacciones y despliegue, pero hace importante preservar la propiedad lógica de cada dato al modificar controladores.
