# 04 — Tabla de trazado C4 → código

## Contenedores

| ID | Elemento | Ancla real | Símbolo / evidencia | Estado |
| --- | --- | --- | --- | --- |
| C2-01 | Aplicación Android | `app/src/main/` | `MainActivity`, `AppNavHost` | ✅ |
| C2-02 | API REST | `backend/src/main/java/.../api/` | `AprendeAprenderApiApplication` | ✅ |
| C2-03 | PostgreSQL 16 | `docker-compose.yml`, `application.yml` | `db`, `jdbc:postgresql` | ✅ |
| C2-04 | Gemma local | `GemmaModelManager.kt`, `GemmaChallengeService.kt` | `generateResponse`, `generarPreguntas` | ✅ |
| C2-05 | UTADEO | `UtadeoService.kt`, `UtadeoRepository.kt` | `sincronizarTodo(...)` | ✅ |

## Componentes backend

| ID | Elemento | Archivo | Símbolo | Relación | Estado |
| --- | --- | --- | --- | --- | --- |
| C3-01 | Seguridad | `SecurityConfig.java`, `TokenAuthenticationFilter.java` | `securityFilterChain`, `doFilterInternal` | filtro → `AuthService.findUserIdByToken` | ✅ |
| C3-02 | AuthController | `AuthController.java` | `register`, `login`, `me`, `logout` | → `AuthService` | ✅ |
| C3-03 | AuthService | `AuthService.java` | `register`, `login`, `createSession` | → `JdbcClient` | ✅ |
| C3-04 | ProfileController | `ProfileController.java` | `get`, `update` | → `JdbcClient` | ✅ |
| C3-05 | SubjectController | `SubjectController.java` | `list`, `create`, `syncUtadeo` | → `JdbcClient` | ✅ |
| C3-06 | TaskController | `TaskController.java` | `list`, `create`, `syncUtadeo` | → `JdbcClient` | ✅ |
| C3-07 | ChallengeController | `ChallengeController.java` | `today`, `complete`, `questions` | → `JdbcClient` | ✅ |

## Walking skeleton — `GET /api/tasks`

1. `ApiService.getTasks()` declara `@GET("api/tasks")`.
2. `ApiClient` crea Retrofit y agrega Bearer.
3. `TokenAuthenticationFilter` valida el token usando `AuthService.findUserIdByToken(...)`.
4. Spring enruta a `TaskController.list(Authentication auth)`.
5. `TaskController` obtiene `userId` desde `auth.getName()`.
6. Ejecuta `jdbc.sql(...)` con `WHERE t.user_id = :userId`.
7. PostgreSQL devuelve filas y Spring serializa JSON.
8. `TaskRepository.getMyTasks()` transforma la respuesta en modelos Android.

## Correcciones

- Flyway administra el esquema; no es un servicio de negocio.
- No existen repositories del backend para perfil/materias/tareas/retos: esos controladores usan `JdbcClient` directamente.
- `OpenRouterService.kt` existe, pero no se incluye como contenedor activo porque el flujo verificado de `ChallengeRepository` usa `GemmaChallengeService` → `GemmaModelManager`.
- Administrador se elimina del contexto as-is mientras no exista un flujo implementado verificable.
