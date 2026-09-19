# 03 — Atributos de calidad

Para este corte nos interesa principalmente seguridad, rendimiento y modificabilidad. Los demás siguen siendo importantes, pero esos tres son los que más se ven en las decisiones actuales.

| Atributo | Prioridad | Por qué |
| --- | --- | --- |
| Seguridad | Alta | hay cuentas, sesiones y datos académicos por usuario |
| Rendimiento | Alta | `GET /api/tasks` puede devolver colecciones grandes |
| Modificabilidad | Alta | Android no debe quedar amarrado a PostgreSQL |
| Reproducibilidad | Alta | la medición y la arquitectura deben poder revisarse |
| Disponibilidad | Media | si API o DB caen no se puede persistir ni consultar |
| Interoperabilidad | Media | UTADEO es una dependencia externa |

## Escenario principal: TASK-PERF-01

**Fuente:** usuarios autenticados representados con k6.

**Estímulo:** 30 usuarios concurrentes consultan `GET /api/tasks`.

**Entorno:** 5.000 usuarios, 5 materias por usuario y 1.000 tareas por usuario.

**Respuesta esperada:** HTTP 200 y exactamente las 1.000 tareas de la cuenta que hizo la petición.

**Medida:** p95 del tiempo HTTP. Se hicieron cuatro corridas de 60 segundos y la primera quedó como calentamiento.

**Resultado:** la mediana de los p95 de las corridas 2–4 fue **90,7544952 ms**.

## Escenario de seguridad

En `TaskController.list()`, el usuario sale de `Authentication` y la consulta usa:

`WHERE t.user_id = :userId`

Ese filtro es la evidencia que usamos para decir que la consulta de tareas queda separada por usuario.

## Decisiones que salen de estos atributos

| Atributo | Decisión actual |
| --- | --- |
| Seguridad | Spring Security, Bearer, BCrypt y hash SHA-256 de sesión |
| Rendimiento | medir antes de meter optimizaciones |
| Modificabilidad | Android → API REST → PostgreSQL |
| Reproducibilidad | Docker Compose, Flyway, k6 y resultados en Git |
