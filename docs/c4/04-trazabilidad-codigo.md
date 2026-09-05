# Trazabilidad C4 hacia el código

La tabla completa, el registro de correcciones y el Walking Skeleton se encuentran en:

`dossier-arquitectura/docs/07-c4-componentes.md`

## Resumen

| Elemento C4 | Evidencia principal | Estado |
| --- | --- | --- |
| Aplicación Android | `app/src/main/` | ✅ Verificado |
| API Spring Boot | `backend/src/main/java/com/example/aprendeaprender/api/` | ✅ Verificado |
| PostgreSQL 16 | `docker-compose.yml`, `application.yml` | ✅ Verificado |
| Seguridad | `SecurityConfig.java`, `TokenAuthenticationFilter.java` | ✅ Verificado |
| AuthService | `AuthService.java` | ✅ Verificado |
| TaskController | `TaskController.java` | ✅ Verificado |
| SubjectController | `SubjectController.java` | ✅ Verificado |
| ProfileController | `ProfileController.java` | ✅ Verificado |
| ChallengeController | `ChallengeController.java` | ✅ Verificado |
| Backend Repository de tareas | no existe | 🗑️ Eliminado |
| JDBC / Flyway como una sola capa | responsabilidades distintas | ✏️ Corregido |

## Walking Skeleton

`TaskRepository.getMyTasks() → ApiService.getTasks() → ApiClient → TokenAuthenticationFilter → AuthService → TaskController.list() → JdbcClient → PostgreSQL`

La representación gráfica y la evidencia de cada salto están documentadas en `dossier-arquitectura/docs/07-c4-componentes.md`.
