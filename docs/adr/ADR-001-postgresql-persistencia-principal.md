# ADR-001 — PostgreSQL como persistencia principal

**Estado:** Aceptada  
**Tipo:** retrospectivo

## Contexto

El proyecto necesitaba una persistencia central para usuarios, sesiones, materias, tareas y demás información que comparte el backend.

Durante la evolución del proyecto hubo persistencia en Firebase, pero para la arquitectura actual necesitábamos una base que pudiéramos consultar directamente desde el backend, versionar con migraciones y usar también en las pruebas de carga.

## Decisión

La persistencia principal queda en PostgreSQL 16.

Android no se conecta directamente a PostgreSQL. El acceso se hace desde la API Spring Boot y actualmente varias operaciones del backend usan JdbcClient.

La base se levanta localmente con Docker Compose y su esquema se mantiene mediante Flyway.

## Alternativas consideradas

### Firebase

Ya se había usado en el proyecto y simplificaba parte de la integración desde Android.

Se dejó de usar como persistencia principal porque la arquitectura actual concentra el acceso a datos en el backend y necesitábamos trabajar con PostgreSQL para consultas, migraciones y mediciones reproducibles.

### PostgreSQL

Nos permite mantener relaciones entre usuarios, materias y tareas, ejecutar SQL desde el backend y reproducir el entorno local con Docker.

## Consecuencias

- La API pasa a ser responsable del acceso a los datos.
- Android no necesita credenciales de PostgreSQL.
- El esquema debe mantenerse de forma controlada.
- Las consultas SQL y su rendimiento pasan a ser responsabilidad del backend.
- Se agrega una dependencia operativa adicional frente a una solución completamente administrada.

## Evidencia actual

- docker-compose.yml
- backend/src/main/resources/application.yml
- backend/src/main/resources/db/migration/
- uso de JdbcClient en los controladores del backend
- experimento de GET /api/tasks sobre PostgreSQL
