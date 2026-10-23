# ADR — Decisiones de arquitectura

Este directorio reúne decisiones que ya afectan la arquitectura actual de Aprende a Aprender.

Los primeros ADR son retrospectivos: la implementación ya existía cuando se escribió el registro. La idea no es cambiar la historia del proyecto, sino dejar claro qué decisión existe, por qué se mantiene y qué consecuencias tiene hoy.

| ADR | Decisión | Estado |
| --- | --- | --- |
| ADR-001 | PostgreSQL como persistencia principal | Aceptada |
| ADR-002 | API REST entre Android y PostgreSQL | Aceptada |
| ADR-003 | Sesiones propias con token Bearer | Aceptada |
| ADR-004 | Gemma local en Android | Aceptada |
| ADR-005 | Flyway para evolucionar el esquema | Aceptada |

Una decisión puede revisarse si cambia el contexto. Si se reemplaza, no se borra el ADR anterior: se marca como reemplazado y se crea otro registro.

## Correspondencia con el sílabo

- **M4 (decisiones 1 y 2):** consultar [revisión de estilos, diseño modular y brechas de mini-comité](../modulo-4/README.md). ADR-001 y ADR-002 son referentes retrospectivos, no actas de comité.
- **M5 (ADR 3 del curso):** [ADR-006, integración de tareas](ADR-006-integracion-tareas-modulo5.md), **propuesto**, con Spike 1 pendiente.
- **M6 (ADR 4 del curso):** [ADR-007, distribución y resiliencia](ADR-007-distribucion-resiliencia-modulo6.md), **borrador**, pendiente de experimento y mini-comité.

Se mantiene la numeración original. No se inventan aprobaciones, ejecuciones ni evaluaciones docentes.
