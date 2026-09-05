# 02 — Stakeholders, drivers y riesgos

## Stakeholders

| Stakeholder | Interés | Influencia | Evidencia / respuesta |
| --- | --- | --- | --- |
| Estudiante | consultar y organizar sus datos | Alta | autenticación y filtros por usuario |
| Equipo de desarrollo | evolucionar sin romper | Alta | API, Flyway y CI |
| Docente / auditor | trazabilidad | Alta | dossier, C4, tabla y resultados |
| UTADEO | fuente externa | Media | `UtadeoService.kt` |
| Operación futura | ejecución reproducible | Media | Docker y healthchecks |

## Restricciones

| Tipo | Restricción | Consecuencia |
| --- | --- | --- |
| Técnica | Android/Kotlin existente | Retrofit y repositorios del cliente |
| Técnica | PostgreSQL 16 | JDBC, SQL y Flyway |
| Técnica | Gemma local | IA principal fuera del backend |
| Seguridad | no exponer credenciales DB | frontera API REST |
| Metodológica | hipótesis ya versionada | no reescribirla tras medir |

## Drivers priorizados

| Prioridad | Driver | Decisión asociada |
| ---: | --- | --- |
| 1 | Integridad y aislamiento de datos | autenticación + SQL por `user_id` |
| 2 | Seguridad de autenticación | BCrypt + Bearer + hash SHA-256 |
| 3 | Rendimiento de consulta de tareas | medir `GET /api/tasks` |
| 4 | Modificabilidad | Android → API → PostgreSQL |
| 5 | Reproducibilidad | Docker, Flyway, k6, scripts y CI |
| 6 | Interoperabilidad | encapsular UTADEO |
| 7 | Continuidad de IA local | Gemma local |

## Riesgos

| ID | Riesgo | Juicio | Acción |
| --- | --- | --- | --- |
| R-01 | colecciones grandes aumentan latencia | Válido | medir antes de optimizar |
| R-02 | mezcla de datos entre usuarios | Válido | conservar filtro por `user_id` |
| R-03 | “PostgreSQL será lento” | Genérico | reformular con operación/carga/p95 |
| R-04 | “se necesita Kubernetes” | Irrelevante | no introducirlo en este corte |
| R-05 | “Android usa PostgreSQL directo” | Falso | demostrar Retrofit → API → JDBC |
| R-06 | cambios de UTADEO | Válido | mantener integración aislada |
