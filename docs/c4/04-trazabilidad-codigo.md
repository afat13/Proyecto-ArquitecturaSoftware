# Trazabilidad C4 hacia el cÃ³digo

| Elemento C4 | ImplementaciÃ³n rastreable |
| --- | --- |
| AplicaciÃ³n Android | `app/src/main/` |
| Cliente HTTP Android | `app/src/main/java/.../data/remote/ApiClient.kt` y `ApiService.kt` |
| SesiÃ³n Android | `SessionStore.kt` |
| API Spring Boot | `backend/src/main/java/` |
| Seguridad | `SecurityConfig.java`, `TokenAuthenticationFilter.java`, `AuthService.java` |
| AutenticaciÃ³n | controlador de autenticaciÃ³n y `AuthService.java` |
| Perfil | controlador/repository de perfil del backend |
| Materias | controlador/repository de materias del backend |
| Tareas | controlador/repository de tareas del backend |
| Retos | controlador/repository de retos del backend |
| PostgreSQL | `docker-compose.yml` y esquema Flyway |
| Esquema relacional | `backend/src/main/resources/db/migration/V1__esquema_relacional.sql` |
| MigraciÃ³n UTADEO | `V2__sincronizacion_utadeo.sql` |
| IA local | clases Android relacionadas con Gemma/LiteRT-LM |
| Experimento de rendimiento | `experimentos/consulta-tareas/` |
| CI | `.github/workflows/ci.yml` |

## Regla arquitectÃ³nica principal

La persistencia sigue `Android -> API REST -> PostgreSQL`. El cliente Android no contiene credenciales ni conexiÃ³n JDBC hacia PostgreSQL.
