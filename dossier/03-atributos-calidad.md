# 03 — Atributos de calidad

## Matriz priorizada

| Orden | Atributo | Impacto | Urgencia | Puntaje | Justificación |
| ---: | --- | ---: | ---: | ---: | --- |
| 1 | Seguridad / aislamiento | 5 | 5 | 25 | cuentas, sesiones y datos académicos |
| 2 | Rendimiento | 5 | 4 | 20 | `GET /api/tasks` devuelve colecciones completas |
| 3 | Modificabilidad | 4 | 4 | 16 | separar Android, API y persistencia |
| 4 | Reproducibilidad | 4 | 4 | 16 | auditoría y repetición del experimento |
| 5 | Disponibilidad | 3 | 3 | 9 | persistencia depende de API + DB |
| 6 | Interoperabilidad | 3 | 3 | 9 | UTADEO es externo |

## TASK-PERF-01 — Consulta concurrente

- Fuente: estudiantes autenticados representados por k6.
- Estímulo: 30 VU ejecutan `GET /api/tasks`.
- Artefacto: seguridad, `TaskController`, `JdbcClient`, PostgreSQL.
- Entorno: 5.000 usuarios, 1.000 tareas por usuario, 5.000.000 totales.
- Respuesta: HTTP 200 y exactamente 1.000 tareas de la identidad autenticada.
- Medida: cuatro corridas de 60 s; corrida 1 calentamiento; línea base = mediana p95 de corridas 2–4.
- Resultado: **90,7544952 ms**.

## TASK-SEC-01 — Aislamiento

La respuesta debe corresponder al usuario autenticado. `TaskController.list()` obtiene el identificador desde `Authentication` y filtra SQL mediante `WHERE t.user_id = :userId`.

## AUTH-SEC-01 — Protección de credenciales

`AuthService` usa BCrypt para contraseñas, genera token aleatorio de sesión y persiste solo el hash SHA-256 del token con expiración.

## Mapa atributo–decisión

| Atributo | Decisión | Evidencia |
| --- | --- | --- |
| Seguridad | Spring Security + Bearer + BCrypt | `SecurityConfig.java`, `TokenAuthenticationFilter.java`, `AuthService.java` |
| Rendimiento | medir antes de optimizar | `experimentos/consulta-tareas/` |
| Modificabilidad | Android → API → PostgreSQL | `ApiClient.kt`, `ApiService.kt`, backend |
| Reproducibilidad | Docker, Flyway, k6 | `docker-compose.yml`, migraciones, experimento |
