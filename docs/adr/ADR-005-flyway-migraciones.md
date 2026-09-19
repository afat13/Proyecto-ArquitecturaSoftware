# ADR-005 — Flyway para controlar cambios del esquema

**Estado:** Aceptada  
**Tipo:** retrospectivo

## Contexto

Al usar PostgreSQL necesitamos una forma repetible de crear y evolucionar el esquema.

Modificar tablas manualmente en cada equipo haría difícil saber qué versión de la base corresponde al código y también complicaría reproducir pruebas.

## Decisión

Los cambios estructurales de PostgreSQL se manejan con Flyway mediante migraciones versionadas dentro del backend.

Spring Boot ejecuta las migraciones ubicadas en:

backend/src/main/resources/db/migration/

Flyway no se modela como un contenedor C4 independiente porque no atiende operaciones de negocio. Su responsabilidad es preparar o evolucionar el esquema.

## Alternativas consideradas

### Cambios manuales

Son rápidos para pruebas pequeñas, pero no dejan una secuencia confiable para los demás integrantes ni para CI.

### Un único script SQL actualizado

Es simple al principio, pero pierde el historial de cómo llegó la base a su estado actual.

### Migraciones versionadas con Flyway

Dejan cada cambio identificado y permiten reconstruir el esquema en otro entorno.

## Consecuencias

- Los cambios del esquema deben entrar como nuevas migraciones.
- No conviene editar migraciones ya aplicadas como si nunca hubieran existido.
- El backend y la estructura de la base quedan más fáciles de reproducir en CI y Docker.
- Flyway agrega disciplina y un paso más al arranque, pero evita depender de cambios manuales.

## Evidencia actual

- configuración spring.flyway en application.yml
- backend/src/main/resources/db/migration/
- arranque del backend sobre PostgreSQL en Docker Compose
