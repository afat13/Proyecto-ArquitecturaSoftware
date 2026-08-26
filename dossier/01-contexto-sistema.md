# Contexto del sistema — Aprende a Aprender

## Propósito

Aprende a Aprender es una aplicación móvil Android orientada a la organización académica y al refuerzo del estudio. El sistema permite a un estudiante administrar materias y tareas, sincronizar información académica disponible en UTADEO, recibir notificaciones y realizar retos de estudio cuyas preguntas pueden ser generadas localmente mediante Gemma.

## Alcance actual

El sistema actual se compone de:

- una aplicación Android desarrollada con Kotlin y Jetpack Compose;
- una API REST desarrollada con Spring Boot;
- PostgreSQL 16 como persistencia principal de usuarios, sesiones, materias, tareas y retos;
- Flyway para versionar el esquema de datos;
- integración externa con servicios académicos de UTADEO;
- un modelo Gemma ejecutado localmente en el dispositivo para generación de preguntas;
- WorkManager para tareas periódicas y trabajos en segundo plano;
- Docker Compose para levantar de forma reproducible la API y PostgreSQL.

## Límite del sistema

La aplicación Android no se conecta directamente a PostgreSQL. La comunicación de persistencia sigue el flujo:

```text
Android
  |
  | HTTP/JSON + token Bearer
  v
API Spring Boot
  |
  | JDBC
  v
PostgreSQL 16
```

UTADEO es un sistema externo. Gemma se ejecuta localmente dentro del dispositivo Android y no pertenece al backend.

## Funcionalidades incluidas

- registro de usuarios nuevos;
- inicio y cierre de sesión;
- consulta y actualización de perfil;
- creación, consulta y eliminación de materias;
- creación, consulta, actualización de estado y eliminación de tareas;
- sincronización de materias, participantes y tareas provenientes de UTADEO;
- persistencia de progreso de retos diarios;
- persistencia de preguntas de retos;
- generación local de preguntas mediante Gemma;
- notificaciones y sincronizaciones periódicas.

## Funcionalidades explícitamente fuera del alcance actual

La recuperación de contraseña y la verificación de correo electrónico no se implementan en esta primera migración. La interfaz heredada puede conservar referencias visuales a estos flujos para facilitar su implementación posterior, pero no forman parte de las capacidades comprometidas para esta línea base.

## Restricciones conocidas

### Tecnológicas

- El cliente principal es Android.
- El backend utiliza Java 21 y Spring Boot 3.3.4.
- La base de datos utilizada para esta etapa es PostgreSQL 16.
- El esquema de datos se versiona mediante Flyway.
- La aplicación Android consume la API mediante HTTP/JSON.
- Las credenciales de PostgreSQL no deben estar presentes dentro de la aplicación Android.
- Gemma debe continuar ejecutándose localmente en el dispositivo.

### De integración

- La sincronización con UTADEO depende de la disponibilidad y del comportamiento del servicio externo.
- Las credenciales académicas del estudiante se almacenan en el dispositivo para poder realizar las sincronizaciones actuales.

### De ejecución

- El entorno reproducible del backend requiere Docker para la ruta recomendada de ejecución.
- Para ejecutar Android se requiere Android SDK 36 y un dispositivo o emulador compatible.

## Hechos verificados en el código

1. Existe una aplicación Android basada en Kotlin y Jetpack Compose.
2. La aplicación contiene gestión de materias, tareas, perfil, retos, chat e integración con UTADEO.
3. La generación de preguntas con Gemma ocurre localmente en Android.
4. El backend Spring Boot expone una API REST y utiliza JDBC.
5. PostgreSQL es la persistencia principal de la arquitectura migrada.
6. Flyway crea y evoluciona el esquema relacional.
7. La autenticación del backend utiliza sesiones mediante token Bearer y almacena solamente el hash SHA-256 del token en la base de datos.
8. Las contraseñas de usuarios se almacenan mediante BCrypt.

## Estado experimental actual

La hipótesis histórica preregistrada se conserva sin modificación y planteaba observar degradación con una cuenta de 10.000 tareas distribuidas de forma desigual. Antes de ejecutar la primera medición real, el diseño experimental se ajustó y quedó documentado separadamente.

El escenario finalmente medido fue:

- 5.000 usuarios experimentales distintos;
- 5 materias por usuario;
- 1.000 tareas por usuario;
- 5.000.000 de tareas totales;
- 30 usuarios virtuales concurrentes, cada uno con una cuenta distinta;
- operación observada: `GET /api/tasks`;
- cuatro corridas de 60 segundos, con la primera tratada como calentamiento;
- línea base: mediana del p95 de las corridas 2, 3 y 4 = aproximadamente 90,75 ms.

Este resultado caracteriza únicamente el escenario revisado bajo las condiciones registradas. No debe presentarse como una medición literal de la semilla original de 10.000 tareas.

## Supuestos pendientes de verificar con evidencia de ejecución

- Que el entorno completo pueda levantarse en las máquinas de los tres integrantes sin ajustes particulares adicionales.
- Que la integración UTADEO mantenga estable el formato de sus respuestas durante el semestre.
- Que el umbral de rendimiento adoptado siga siendo adecuado al ampliar o modificar el escenario de carga.
- Que el comportamiento observado se mantenga en hardware, red o configuraciones distintas a las registradas en la línea base.

Los supuestos anteriores no se presentan como hechos hasta que exista evidencia reproducible.

## Riesgos iniciales

| Riesgo | Impacto | Tratamiento inicial |
| --- | --- | --- |
| Cambio o indisponibilidad de UTADEO | La sincronización puede fallar | Aislar la integración en un repositorio/servicio independiente |
| Crecimiento del número de tareas | Puede aumentar la latencia de consulta | Medir una línea base con una semilla controlada |
| Exposición de credenciales o tokens | Compromiso de cuentas | Variables de entorno, BCrypt y hash de tokens |
| Dependencia de recursos del dispositivo para Gemma | Generación lenta o no disponible | Mantener descarga/ejecución separada de la persistencia del backend |
| Divergencia entre esquema y código | Fallos de despliegue | Migraciones Flyway versionadas |
| Cambios que rompan Android o backend | Regresiones | Pruebas automatizadas y CI en GitHub Actions |
