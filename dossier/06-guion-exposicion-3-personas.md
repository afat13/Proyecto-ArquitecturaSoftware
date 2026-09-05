# 06 — Guion de exposición para tres personas

## Persona 1 — Contexto y contenedores

Mostrar `01-contexto-sistema.md`, `01-contexto.puml`, `02-contenedores.puml` y `docker-compose.yml`.

Mensaje central: Android consume una API Spring Boot; la API usa PostgreSQL 16; Gemma corre localmente; UTADEO es externo.

## Persona 2 — Componentes y trazabilidad

Mostrar `03-componentes.puml`, `04-trazabilidad-codigo.md`, `SecurityConfig.java`, `TokenAuthenticationFilter.java` y `TaskController.java`.

Walking skeleton:

```text
ApiService.getTasks()
 -> GET /api/tasks
 -> TokenAuthenticationFilter
 -> TaskController.list()
 -> JdbcClient.sql(...)
 -> PostgreSQL
```

Punto clave: los controladores de perfil, materias, tareas y retos usan `JdbcClient` directamente; no se inventa una capa repository del backend.

## Persona 3 — Experimento

Mostrar hipótesis, protocolo, resultado y `resultado.json`.

Mensaje central: la hipótesis original usaba 10.000 tareas para una cuenta; el diseño medido cambió antes de la primera corrida a 5.000 cuentas con 1.000 tareas cada una. La línea base del diseño revisado es **90,7544952 ms**.

Evitar decir que la hipótesis original quedó confirmada, que PostgreSQL “causó” el resultado o que OpenRouter forma parte del flujo actual sin evidencia.
