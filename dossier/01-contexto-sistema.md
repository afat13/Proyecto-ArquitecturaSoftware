# 01 — Contexto del sistema

## Qué es Aprende a Aprender

Aprende a Aprender es una aplicación Android para organizar materias y tareas y reforzar el estudio con retos. El proyecto ya existía antes de esta materia y en este corte migramos la persistencia principal a una API Spring Boot con PostgreSQL.

El estudiante usa la aplicación Android. Desde ahí puede iniciar sesión, manejar materias y tareas, sincronizar información de UTADEO y hacer retos.

## Cómo está dividido

```text
Estudiante
   |
   v
Android
   | HTTP/JSON + Bearer
   v
API Spring Boot
   | JDBC
   v
PostgreSQL 16

Android ---> UTADEO
Android ---> Gemma local
```

La regla más importante de esta arquitectura es que Android no se conecta directamente a PostgreSQL. La aplicación consume la API y la API es la que consulta la base.

## Qué sí está implementado

- registro, login y logout;
- perfil;
- materias y participantes;
- tareas;
- sincronización con UTADEO;
- progreso de retos y preguntas;
- generación de preguntas con Gemma local;
- WorkManager para trabajos en segundo plano;
- migraciones Flyway;
- CI para Android y backend.

## Qué no estamos presentando como parte del sistema actual

No incluimos recuperación de contraseña ni verificación de correo porque todavía no están implementadas.

Tampoco dejamos un actor Administrador en el C4. En versiones anteriores apareció, pero al revisar el código no encontramos un flujo administrativo real que pudiéramos mostrar en la defensa.

OpenRouter tiene código en el proyecto, pero el flujo de retos que pudimos seguir desde `ChallengeRepository` usa `GemmaChallengeService` y `GemmaModelManager`. Por eso no lo estamos dibujando como parte del flujo principal as-is.

## Dependencias externas

UTADEO está fuera de nuestro control. Si cambia su formato o deja de responder, la sincronización puede fallar.

Gemma se ejecuta localmente en Android, así que también depende de los recursos del dispositivo y de que el modelo esté disponible.

## Línea base que ya medimos

La operación que usamos para la medición fue:

`GET /api/tasks`

La prueba final quedó con:

- 5.000 usuarios;
- 5 materias por usuario;
- 1.000 tareas por usuario;
- 5.000.000 de tareas en total;
- 30 VU;
- 4 corridas de 60 segundos;
- corrida 1 usada como calentamiento.

La mediana del p95 de las corridas 2, 3 y 4 fue **90,7544952 ms**.

La primera hipótesis hablaba de un usuario con 10.000 tareas. Esa hipótesis se conserva porque fue registrada antes, pero no fue exactamente el escenario que terminamos midiendo.
