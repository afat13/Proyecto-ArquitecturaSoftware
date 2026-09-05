# 01 — Contexto del sistema

Estado: **sistema existente, migrado a API REST + PostgreSQL y con línea base experimental registrada**.

## Sistema base

**Aprende a Aprender** es una aplicación Android para organización académica y refuerzo del estudio. Permite registro, sesión, perfil, materias, tareas, sincronización con UTADEO y retos de estudio. La generación principal de preguntas del flujo actual utiliza Gemma local en Android.

## Actores y sistemas externos

| Actor / sistema | Objetivo | Relación |
| --- | --- | --- |
| Estudiante | Organizar materias, tareas y estudio | Usa Android |
| UTADEO | Proveer información académica | Android consulta y transforma información externa |
| Gemma local | Generar preguntas | Android ejecuta inferencia local |
| Equipo de desarrollo | Mantener y medir | Pruebas, CI, migraciones y experimento |
| Docente / auditor | Verificar decisiones | C4, código, dossier y resultados |

No se incluye Administrador en el C4 as-is porque no se encontró un flujo implementado verificable equivalente al descrito en documentos tempranos.

## Límite del sistema

```text
Estudiante
    |
    v
Android (Kotlin + Jetpack Compose)
    | HTTP/JSON + Bearer
    v
API REST (Spring Boot 3.3.4 / Java 21)
    | JDBC mediante Spring JdbcClient
    v
PostgreSQL 16

Android ---> UTADEO
Android ---> Gemma local / LiteRT-LM
```

Android no contiene una conexión JDBC ni credenciales de PostgreSQL.

## Capacidades verificadas

- registro, login y logout;
- perfil;
- materias, participantes y tareas;
- sincronización UTADEO;
- progreso y preguntas de retos;
- generación local con Gemma;
- WorkManager y notificaciones;
- Flyway;
- CI para backend y Android.

## Fuera del alcance

- recuperación de contraseña;
- verificación de correo;
- alta disponibilidad o multi-región;
- conexión directa Android → PostgreSQL;
- presentar OpenRouter como flujo activo sin una llamada verificada.

## Línea base experimental

Operación: `GET /api/tasks`.

Diseño medido: 5.000 usuarios, 5 materias por usuario, 1.000 tareas por usuario, 5.000.000 de tareas, 30 VU con identidades distintas y 4 corridas de 60 s. La corrida 1 fue calentamiento. La mediana del p95 de corridas 2–4 fue **90,7544952 ms**.

La hipótesis original de un usuario con 10.000 tareas se conserva por separado y no debe presentarse como si fuera la semilla realmente medida.
